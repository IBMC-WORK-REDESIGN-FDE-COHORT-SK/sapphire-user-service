package com.sapphire.userservice.service;

import com.sapphire.userservice.model.PromotionEntity;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.repository.PromotionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PromotionValidationService}.
 * Targets 100% line coverage as required by JaCoCo rule (SC-006).
 */
@ExtendWith(MockitoExtension.class)
class PromotionValidationServiceTest {

    private static final Instant NOW   = Instant.now();
    private static final Instant LATER = NOW.plusSeconds(3600);

    @Mock
    private PromotionRepository promotionRepository;

    @InjectMocks
    private PromotionValidationService validationService;

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — happy path
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_succeeds_withValidInputs() {
        when(promotionRepository.findOtherActiveForTier(eq(UserTier.FREE), any())).thenReturn(Optional.empty());

        assertThatCode(() -> validationService.validateCreate(
                "Upgrade Now", "SAVE 20%", "Upgrade",
                "https://example.com/upgrade", NOW, LATER,
                UserTier.FREE, true))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_succeeds_withMaxLengthTitle() {
        String maxTitle = "A".repeat(80);
        when(promotionRepository.findOtherActiveForTier(eq(UserTier.FREE), any())).thenReturn(Optional.empty());

        assertThatCode(() -> validationService.validateCreate(
                maxTitle, null, "Go Premium",
                "https://example.com", NOW, LATER,
                UserTier.FREE, true))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_succeeds_withInactivePromotion() {
        // No conflict check performed when isActive = false
        assertThatCode(() -> validationService.validateCreate(
                "Offer", null, "Upgrade",
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .doesNotThrowAnyException();
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — title validation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_throwsException_whenTitleIsNull() {
        assertThatThrownBy(() -> validationService.validateCreate(
                null, null, "Go",
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("title is required");
    }

    @Test
    void validateCreate_throwsException_whenTitleIsBlank() {
        assertThatThrownBy(() -> validationService.validateCreate(
                "   ", null, "Go",
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("title is required");
    }

    @Test
    void validateCreate_throwsException_whenTitleExceedsMaxLength() {
        String longTitle = "A".repeat(81);
        assertThatThrownBy(() -> validationService.validateCreate(
                longTitle, null, "Go",
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("title exceeds maximum length of 80");
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — badgeLabel validation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_succeeds_withMaxLengthBadge() {
        String maxBadge = "B".repeat(20);
        when(promotionRepository.findOtherActiveForTier(eq(UserTier.FREE), any())).thenReturn(Optional.empty());

        assertThatCode(() -> validationService.validateCreate(
                "Title", maxBadge, "Go",
                "https://example.com", NOW, LATER,
                UserTier.FREE, true))
                .doesNotThrowAnyException();
    }

    @Test
    void validateCreate_throwsException_whenBadgeLabelExceedsMaxLength() {
        String longBadge = "B".repeat(21);
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", longBadge, "Go",
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("badgeLabel exceeds maximum length of 20");
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — ctaLabel validation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_throwsException_whenCtaLabelIsNull() {
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, null,
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("ctaLabel is required");
    }

    @Test
    void validateCreate_throwsException_whenCtaLabelExceedsMaxLength() {
        String longLabel = "C".repeat(31);
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, longLabel,
                "https://example.com", NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("ctaLabel exceeds maximum length of 30");
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — ctaUrl validation
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_throwsException_whenCtaUrlIsNull() {
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                null, NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("ctaUrl is required");
    }

    @Test
    void validateCreate_throwsException_whenCtaUrlExceedsMaxLength() {
        String longUrl = "https://example.com/" + "x".repeat(2030);
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                longUrl, NOW, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("ctaUrl exceeds maximum length of 2048");
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — window validation (FR-005a)
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_throwsException_whenStartsAtIsNull() {
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                "https://example.com", null, LATER,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("startsAt is required");
    }

    @Test
    void validateCreate_throwsException_whenExpiresAtIsNull() {
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                "https://example.com", NOW, null,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("expiresAt is required");
    }

    @Test
    void validateCreate_throwsException_whenExpiresAtEqualsStartsAt() {
        // FR-005a: zero-length window is never valid
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                "https://example.com", NOW, NOW,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("expiresAt must be strictly after startsAt");
    }

    @Test
    void validateCreate_throwsException_whenExpiresAtBeforeStartsAt() {
        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                "https://example.com", LATER, NOW,
                UserTier.FREE, false))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("expiresAt must be strictly after startsAt");
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateCreate — single-active-per-tier conflict (FR-009)
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateCreate_throwsConflict_whenActivePromotionExistsForTier() {
        PromotionEntity existing = new PromotionEntity();
        when(promotionRepository.findOtherActiveForTier(eq(UserTier.FREE), any()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validationService.validateCreate(
                "Title", null, "Go",
                "https://example.com", NOW, LATER,
                UserTier.FREE, true))
                .isInstanceOf(ActivePromotionConflictException.class)
                .hasMessageContaining("FREE");
    }

    // ────────────────────────────────────────────────────────────────────────
    // validateUpdate — partial-update rules
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void validateUpdate_succeeds_whenAllFieldsNull() {
        UUID id = UUID.randomUUID();
        assertThatCode(() -> validationService.validateUpdate(
                id, null, null, null, null, null, null, null, null))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_throwsException_whenTitleExceedsMaxLength() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, "A".repeat(81), null, null, null,
                null, null, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("title exceeds maximum length of 80");
    }

    @Test
    void validateUpdate_throwsException_whenBadgeLabelExceedsMaxLength() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, "B".repeat(21), null, null,
                null, null, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("badgeLabel exceeds maximum length of 20");
    }

    @Test
    void validateUpdate_throwsException_whenCtaLabelExceedsMaxLength() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, null, "C".repeat(31), null,
                null, null, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("ctaLabel exceeds maximum length of 30");
    }

    @Test
    void validateUpdate_throwsException_whenCtaUrlExceedsMaxLength() {
        UUID id = UUID.randomUUID();
        String longUrl = "https://example.com/" + "x".repeat(2030);
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, null, null, longUrl,
                null, null, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("ctaUrl exceeds maximum length of 2048");
    }

    @Test
    void validateUpdate_throwsException_whenOnlyStartsAtProvided() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, null, null, null,
                NOW, null, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("both be provided");
    }

    @Test
    void validateUpdate_throwsException_whenOnlyExpiresAtProvided() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, null, null, null,
                null, LATER, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("both be provided");
    }

    @Test
    void validateUpdate_throwsException_whenWindowIsInvalid() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, null, null, null,
                LATER, NOW, null, null))
                .isInstanceOf(PromotionValidationException.class)
                .hasMessageContaining("expiresAt must be strictly after startsAt");
    }

    @Test
    void validateUpdate_throwsConflict_whenActivatingWithExistingActivePromotion() {
        UUID id = UUID.randomUUID();
        PromotionEntity existing = new PromotionEntity();
        when(promotionRepository.findOtherActiveForTier(eq(UserTier.FREE), eq(id)))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> validationService.validateUpdate(
                id, null, null, null, null,
                null, null, UserTier.FREE, true))
                .isInstanceOf(ActivePromotionConflictException.class)
                .hasMessageContaining("FREE");
    }

    @Test
    void validateUpdate_noConflictCheck_whenDeactivating() {
        UUID id = UUID.randomUUID();
        // active = false → no conflict check, no repo call needed
        assertThatCode(() -> validationService.validateUpdate(
                id, null, null, null, null,
                null, null, UserTier.FREE, false))
                .doesNotThrowAnyException();
    }

    @Test
    void validateUpdate_noConflictCheck_whenActiveIsNull() {
        UUID id = UUID.randomUUID();
        assertThatCode(() -> validationService.validateUpdate(
                id, null, null, null, null,
                null, null, UserTier.FREE, null))
                .doesNotThrowAnyException();
    }
}
