package com.sapphire.userservice.service;

import com.sapphire.userservice.dto.ActivePromotionResponse;
import com.sapphire.userservice.dto.CreatePromotionRequest;
import com.sapphire.userservice.dto.UpdatePromotionRequest;
import com.sapphire.userservice.metrics.PromotionMetrics;
import com.sapphire.userservice.model.PromotionEntity;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class PromotionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionService.class);

    private final PromotionRepository promotionRepository;
    private final PromotionValidationService validationService;
    private final PromotionMetrics promotionMetrics;

    public PromotionService(
            PromotionRepository promotionRepository,
            PromotionValidationService validationService,
            PromotionMetrics promotionMetrics) {
        this.promotionRepository = promotionRepository;
        this.validationService = validationService;
        this.promotionMetrics = promotionMetrics;
    }

    /**
     * Returns the active promotion for FREE-tier users, or empty if none exists
     * or if the current time is outside the promotion window (FR-004, FR-005).
     */
    @Transactional(readOnly = true)
    public Optional<ActivePromotionResponse> getActivePromotion(UserTier tier) {
        Instant now = Instant.now();
        Optional<PromotionEntity> found = promotionRepository.findActiveForTier(tier, now);
        found.ifPresent(p -> {
            try {
                promotionMetrics.incrementImpressionCounter(p.getTargetTier().name());
            } catch (Exception ex) {
                // FR-018: counter failures must be non-blocking
                LOGGER.warn("Failed to record impression metric for promotion={}", p.getId(), ex);
            }
        });
        return found.map(this::toResponse);
    }

    /**
     * Creates a new promotion. Delegates validation to PromotionValidationService.
     */
    @Transactional
    public ActivePromotionResponse createPromotion(CreatePromotionRequest req) {
        LOGGER.info("Creating promotion title='{}' tier={}", req.getTitle(), req.getTargetTier());

        UserTier tier = parseTier(req.getTargetTier());

        validationService.validateCreate(
                req.getTitle(),
                req.getBadgeLabel(),
                req.getCtaLabel(),
                req.getCtaUrl(),
                req.getStartsAt(),
                req.getExpiresAt(),
                tier,
                req.isActive());

        PromotionEntity entity = new PromotionEntity();
        entity.setTitle(req.getTitle());
        entity.setBadgeLabel(req.getBadgeLabel());
        entity.setBodyText(req.getBodyText());
        entity.setCtaLabel(req.getCtaLabel());
        entity.setCtaUrl(req.getCtaUrl());
        entity.setBackgroundColour(req.getBackgroundColour() != null ? req.getBackgroundColour() : "#FFFFFF");
        entity.setTextColour(req.getTextColour() != null ? req.getTextColour() : "#000000");
        entity.setTargetTier(tier);
        entity.setStartsAt(req.getStartsAt());
        entity.setExpiresAt(req.getExpiresAt());
        entity.setActive(req.isActive());

        PromotionEntity saved = promotionRepository.save(entity);
        LOGGER.info("Created promotion id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Partially updates an existing promotion. Only non-null fields in the
     * request are applied (PATCH semantics).
     */
    @Transactional
    public ActivePromotionResponse updatePromotion(UUID id, UpdatePromotionRequest req) {
        LOGGER.info("Updating promotion id={}", id);

        PromotionEntity entity = promotionRepository.findById(id)
                .orElseThrow(() -> new PromotionNotFoundException(id));

        UserTier resolvedTier = req.getTargetTier() != null
                ? parseTier(req.getTargetTier())
                : entity.getTargetTier();

        validationService.validateUpdate(
                id,
                req.getTitle(),
                req.getBadgeLabel(),
                req.getCtaLabel(),
                req.getCtaUrl(),
                req.getStartsAt(),
                req.getExpiresAt(),
                resolvedTier,
                req.getActive());

        if (req.getTitle() != null)           entity.setTitle(req.getTitle());
        if (req.getBadgeLabel() != null)      entity.setBadgeLabel(req.getBadgeLabel());
        if (req.getBodyText() != null)        entity.setBodyText(req.getBodyText());
        if (req.getCtaLabel() != null)        entity.setCtaLabel(req.getCtaLabel());
        if (req.getCtaUrl() != null)          entity.setCtaUrl(req.getCtaUrl());
        if (req.getBackgroundColour() != null) entity.setBackgroundColour(req.getBackgroundColour());
        if (req.getTextColour() != null)      entity.setTextColour(req.getTextColour());
        if (req.getTargetTier() != null)      entity.setTargetTier(resolvedTier);
        if (req.getStartsAt() != null)        entity.setStartsAt(req.getStartsAt());
        if (req.getExpiresAt() != null)       entity.setExpiresAt(req.getExpiresAt());
        if (req.getActive() != null)          entity.setActive(req.getActive());

        PromotionEntity saved = promotionRepository.save(entity);
        LOGGER.info("Updated promotion id={}", saved.getId());
        return toResponse(saved);
    }

    /**
     * Records a click event for a promotion. Non-blocking: counter failures
     * are logged but do not propagate (FR-018).
     */
    @Transactional
    public void recordClick(UUID id, String tier) {
        promotionRepository.findById(id).ifPresent(entity -> {
            entity.setClickCount(entity.getClickCount() + 1);
            promotionRepository.save(entity);
        });
        try {
            promotionMetrics.incrementClickCounter(tier);
        } catch (Exception ex) {
            LOGGER.warn("Failed to record click metric for promotion={}", id, ex);
        }
    }

    /**
     * Records a dismiss event for a promotion. Non-blocking (FR-018).
     */
    @Transactional
    public void recordDismiss(UUID id, String tier) {
        promotionRepository.findById(id).ifPresent(entity -> {
            entity.setDismissCount(entity.getDismissCount() + 1);
            promotionRepository.save(entity);
        });
        try {
            promotionMetrics.incrementDismissCounter(tier);
        } catch (Exception ex) {
            LOGGER.warn("Failed to record dismiss metric for promotion={}", id, ex);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private ActivePromotionResponse toResponse(PromotionEntity p) {
        return new ActivePromotionResponse(
                p.getId(),
                p.getTitle(),
                p.getBadgeLabel(),
                p.getBodyText(),
                p.getCtaLabel(),
                p.getCtaUrl(),
                p.getBackgroundColour(),
                p.getTextColour(),
                p.getTargetTier().name(),
                p.getStartsAt(),
                p.getExpiresAt());
    }

    private UserTier parseTier(String raw) {
        try {
            return UserTier.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new PromotionValidationException("Invalid targetTier: " + raw);
        }
    }
}
