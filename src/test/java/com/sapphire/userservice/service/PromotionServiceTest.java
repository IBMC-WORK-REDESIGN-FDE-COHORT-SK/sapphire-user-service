package com.sapphire.userservice.service;

import com.sapphire.userservice.dto.ActivePromotionResponse;
import com.sapphire.userservice.dto.CreatePromotionRequest;
import com.sapphire.userservice.dto.UpdatePromotionRequest;
import com.sapphire.userservice.metrics.PromotionMetrics;
import com.sapphire.userservice.model.PromotionEntity;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PromotionService}.
 */
@ExtendWith(MockitoExtension.class)
class PromotionServiceTest {

    private static final Instant NOW   = Instant.now();
    private static final Instant LATER = NOW.plusSeconds(3600);

    @Mock
    private PromotionRepository promotionRepository;

    @Mock
    private PromotionValidationService validationService;

    @Mock
    private PromotionMetrics promotionMetrics;

    @InjectMocks
    private PromotionService promotionService;

    // ────────────────────────────────────────────────────────────────────────
    // getActivePromotion
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void getActivePromotion_returnsResponse_whenActivePromotionExists() {
        PromotionEntity entity = buildEntity(UUID.randomUUID(), "Summer Deal");
        when(promotionRepository.findActiveForTier(eq(UserTier.FREE), any(Instant.class)))
                .thenReturn(Optional.of(entity));

        Optional<ActivePromotionResponse> result = promotionService.getActivePromotion(UserTier.FREE);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Summer Deal");
        verify(promotionMetrics).incrementImpressionCounter("FREE");
    }

    @Test
    void getActivePromotion_returnsEmpty_whenNoActivePromotion() {
        when(promotionRepository.findActiveForTier(eq(UserTier.FREE), any(Instant.class)))
                .thenReturn(Optional.empty());

        Optional<ActivePromotionResponse> result = promotionService.getActivePromotion(UserTier.FREE);

        assertThat(result).isEmpty();
        verifyNoInteractions(promotionMetrics);
    }

    @Test
    void getActivePromotion_doesNotThrow_whenMetricsFails() {
        PromotionEntity entity = buildEntity(UUID.randomUUID(), "Deal");
        when(promotionRepository.findActiveForTier(eq(UserTier.FREE), any(Instant.class)))
                .thenReturn(Optional.of(entity));
        doThrow(new RuntimeException("metrics unavailable"))
                .when(promotionMetrics).incrementImpressionCounter(any());

        // FR-018: counter failure must not propagate
        Optional<ActivePromotionResponse> result = promotionService.getActivePromotion(UserTier.FREE);
        assertThat(result).isPresent();
    }

    // ────────────────────────────────────────────────────────────────────────
    // createPromotion
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void createPromotion_savesAndReturnsResponse() {
        CreatePromotionRequest req = buildCreateRequest("New Offer", true);
        PromotionEntity saved = buildEntity(UUID.randomUUID(), "New Offer");
        when(promotionRepository.save(any(PromotionEntity.class))).thenReturn(saved);

        ActivePromotionResponse result = promotionService.createPromotion(req);

        assertThat(result.getTitle()).isEqualTo("New Offer");
        verify(validationService).validateCreate(
                eq("New Offer"), any(), any(), any(), any(), any(),
                eq(UserTier.FREE), eq(true));
        verify(promotionRepository).save(any(PromotionEntity.class));
    }

    @Test
    void createPromotion_throwsValidationException_whenValidationFails() {
        CreatePromotionRequest req = buildCreateRequest("", false);
        doThrow(new PromotionValidationException("title is required"))
                .when(validationService).validateCreate(any(), any(), any(), any(), any(), any(), any(), anyBoolean());

        assertThatThrownBy(() -> promotionService.createPromotion(req))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("title is required");
        verify(promotionRepository, never()).save(any());
    }

    @Test
    void createPromotion_throwsForInvalidTier() {
        CreatePromotionRequest req = buildCreateRequest("Offer", false);
        req.setTargetTier("GOLD"); // invalid tier

        assertThatThrownBy(() -> promotionService.createPromotion(req))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("Invalid targetTier");
    }

    // ────────────────────────────────────────────────────────────────────────
    // updatePromotion
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void updatePromotion_appliesNonNullFields() {
        UUID id = UUID.randomUUID();
        PromotionEntity existing = buildEntity(id, "Old Title");
        PromotionEntity saved = buildEntity(id, "New Title");

        when(promotionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(promotionRepository.save(any())).thenReturn(saved);

        UpdatePromotionRequest req = new UpdatePromotionRequest();
        req.setTitle("New Title");

        ActivePromotionResponse result = promotionService.updatePromotion(id, req);

        assertThat(result.getTitle()).isEqualTo("New Title");
        verify(promotionRepository).save(existing);
    }

    @Test
    void updatePromotion_throwsNotFoundException_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(promotionRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> promotionService.updatePromotion(id, new UpdatePromotionRequest()))
                .isInstanceOf(PromotionNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @Test
    void updatePromotion_skipsNullFields() {
        UUID id = UUID.randomUUID();
        PromotionEntity existing = buildEntity(id, "Original");
        when(promotionRepository.findById(id)).thenReturn(Optional.of(existing));
        when(promotionRepository.save(any())).thenReturn(existing);

        // Empty request — nothing changes
        promotionService.updatePromotion(id, new UpdatePromotionRequest());

        assertThat(existing.getTitle()).isEqualTo("Original");
    }

    // ────────────────────────────────────────────────────────────────────────
    // recordClick / recordDismiss (FR-018: non-blocking)
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void recordClick_incrementsCounterAndSaves() {
        UUID id = UUID.randomUUID();
        PromotionEntity entity = buildEntity(id, "Offer");
        when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));
        when(promotionRepository.save(any())).thenReturn(entity);

        promotionService.recordClick(id, "FREE");

        assertThat(entity.getClickCount()).isEqualTo(1L);
        verify(promotionMetrics).incrementClickCounter("FREE");
    }

    @Test
    void recordClick_doesNotThrow_whenMetricsFails() {
        UUID id = UUID.randomUUID();
        PromotionEntity entity = buildEntity(id, "Offer");
        when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));
        when(promotionRepository.save(any())).thenReturn(entity);
        doThrow(new RuntimeException("metrics down")).when(promotionMetrics).incrementClickCounter(any());

        // Must not propagate
        promotionService.recordClick(id, "FREE");
    }

    @Test
    void recordDismiss_incrementsCounterAndSaves() {
        UUID id = UUID.randomUUID();
        PromotionEntity entity = buildEntity(id, "Offer");
        when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));
        when(promotionRepository.save(any())).thenReturn(entity);

        promotionService.recordDismiss(id, "FREE");

        assertThat(entity.getDismissCount()).isEqualTo(1L);
        verify(promotionMetrics).incrementDismissCounter("FREE");
    }

    @Test
    void recordDismiss_doesNotThrow_whenMetricsFails() {
        UUID id = UUID.randomUUID();
        PromotionEntity entity = buildEntity(id, "Offer");
        when(promotionRepository.findById(id)).thenReturn(Optional.of(entity));
        when(promotionRepository.save(any())).thenReturn(entity);
        doThrow(new RuntimeException("metrics down")).when(promotionMetrics).incrementDismissCounter(any());

        promotionService.recordDismiss(id, "FREE");
    }

    // ────────────────────────────────────────────────────────────────────────
    // helpers
    // ────────────────────────────────────────────────────────────────────────

    private PromotionEntity buildEntity(UUID id, String title) {
        PromotionEntity entity = new PromotionEntity();
        ReflectionTestUtils.setField(entity, "id", id);
        entity.setTitle(title);
        entity.setBadgeLabel("20% OFF");
        entity.setCtaLabel("Upgrade");
        entity.setCtaUrl("https://example.com/upgrade");
        entity.setTargetTier(UserTier.FREE);
        entity.setStartsAt(NOW);
        entity.setExpiresAt(LATER);
        entity.setActive(true);
        return entity;
    }

    private CreatePromotionRequest buildCreateRequest(String title, boolean active) {
        CreatePromotionRequest req = new CreatePromotionRequest();
        req.setTitle(title);
        req.setBadgeLabel("SAVE");
        req.setCtaLabel("Upgrade");
        req.setCtaUrl("https://example.com/upgrade");
        req.setTargetTier("FREE");
        req.setStartsAt(NOW);
        req.setExpiresAt(LATER);
        req.setActive(active);
        return req;
    }
}
