package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.repository.PromotionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Validates promotion create/update payloads against all FR rules before
 * any persistence operation. Keeps PromotionService focused on orchestration.
 */
@Service
public class PromotionValidationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionValidationService.class);

    private static final int TITLE_MAX = 80;
    private static final int BADGE_MAX = 20;
    private static final int CTA_LABEL_MAX = 30;
    private static final int CTA_URL_MAX = 2048;

    private final PromotionRepository promotionRepository;

    public PromotionValidationService(PromotionRepository promotionRepository) {
        this.promotionRepository = promotionRepository;
    }

    /**
     * Validates fields and uniqueness constraints for a new promotion.
     *
     * @param title           promotion title (max 80 chars)
     * @param badgeLabel      optional badge text (max 20 chars)
     * @param ctaLabel        CTA button label (max 30 chars)
     * @param ctaUrl          CTA destination URL (max 2048 chars)
     * @param startsAt        window start
     * @param expiresAt       window end — must be strictly after startsAt (FR-005a)
     * @param targetTier      tier this promotion targets
     * @param isActive        whether the promotion should be immediately active
     */
    public void validateCreate(
            String title,
            String badgeLabel,
            String ctaLabel,
            String ctaUrl,
            Instant startsAt,
            Instant expiresAt,
            UserTier targetTier,
            boolean isActive) {

        validateFieldLengths(title, badgeLabel, ctaLabel, ctaUrl);
        validateWindow(startsAt, expiresAt);

        if (isActive) {
            checkNoExistingActivePromotion(targetTier, null);
        }
    }

    /**
     * Validates fields and uniqueness constraints for an update to an existing promotion.
     *
     * @param promotionId the ID of the promotion being updated (excluded from conflict check)
     * @param title       nullable — only validated when non-null
     * @param badgeLabel  nullable — only validated when non-null
     * @param ctaLabel    nullable — only validated when non-null
     * @param ctaUrl      nullable — only validated when non-null
     * @param startsAt    nullable — validated together with expiresAt when either is provided
     * @param expiresAt   nullable — validated together with startsAt when either is provided
     * @param targetTier  nullable — tier for conflict check when activating
     * @param isActive    nullable — when transitioning to true, conflict check is performed
     */
    public void validateUpdate(
            UUID promotionId,
            String title,
            String badgeLabel,
            String ctaLabel,
            String ctaUrl,
            Instant startsAt,
            Instant expiresAt,
            UserTier targetTier,
            Boolean isActive) {

        if (title != null && title.length() > TITLE_MAX) {
            throw new PromotionValidationException(
                    "title exceeds maximum length of " + TITLE_MAX + " characters");
        }
        if (badgeLabel != null && badgeLabel.length() > BADGE_MAX) {
            throw new PromotionValidationException(
                    "badgeLabel exceeds maximum length of " + BADGE_MAX + " characters");
        }
        if (ctaLabel != null && ctaLabel.length() > CTA_LABEL_MAX) {
            throw new PromotionValidationException(
                    "ctaLabel exceeds maximum length of " + CTA_LABEL_MAX + " characters");
        }
        if (ctaUrl != null && ctaUrl.length() > CTA_URL_MAX) {
            throw new PromotionValidationException(
                    "ctaUrl exceeds maximum length of " + CTA_URL_MAX + " characters");
        }

        if (startsAt != null || expiresAt != null) {
            if (startsAt == null || expiresAt == null) {
                throw new PromotionValidationException(
                        "startsAt and expiresAt must both be provided when updating the promotion window");
            }
            validateWindow(startsAt, expiresAt);
        }

        if (Boolean.TRUE.equals(isActive) && targetTier != null) {
            checkNoExistingActivePromotion(targetTier, promotionId);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validateFieldLengths(String title, String badgeLabel, String ctaLabel, String ctaUrl) {
        if (title == null || title.isBlank()) {
            throw new PromotionValidationException("title is required");
        }
        if (title.length() > TITLE_MAX) {
            throw new PromotionValidationException(
                    "title exceeds maximum length of " + TITLE_MAX + " characters");
        }
        if (badgeLabel != null && badgeLabel.length() > BADGE_MAX) {
            throw new PromotionValidationException(
                    "badgeLabel exceeds maximum length of " + BADGE_MAX + " characters");
        }
        if (ctaLabel == null || ctaLabel.isBlank()) {
            throw new PromotionValidationException("ctaLabel is required");
        }
        if (ctaLabel.length() > CTA_LABEL_MAX) {
            throw new PromotionValidationException(
                    "ctaLabel exceeds maximum length of " + CTA_LABEL_MAX + " characters");
        }
        if (ctaUrl == null || ctaUrl.isBlank()) {
            throw new PromotionValidationException("ctaUrl is required");
        }
        if (ctaUrl.length() > CTA_URL_MAX) {
            throw new PromotionValidationException(
                    "ctaUrl exceeds maximum length of " + CTA_URL_MAX + " characters");
        }
    }

    private void validateWindow(Instant startsAt, Instant expiresAt) {
        if (startsAt == null) {
            throw new PromotionValidationException("startsAt is required");
        }
        if (expiresAt == null) {
            throw new PromotionValidationException("expiresAt is required");
        }
        // FR-005a: startsAt == expiresAt is a zero-length window — never valid
        if (!expiresAt.isAfter(startsAt)) {
            throw new PromotionValidationException(
                    "expiresAt must be strictly after startsAt");
        }
    }

    private void checkNoExistingActivePromotion(UserTier tier, UUID excludeId) {
        UUID effectiveExclude = excludeId != null ? excludeId : UUID.fromString("00000000-0000-0000-0000-000000000000");
        promotionRepository.findOtherActiveForTier(tier, effectiveExclude)
                .ifPresent(existing -> {
                    LOGGER.warn("Active promotion conflict for tier={} existingId={}", tier, existing.getId());
                    throw new ActivePromotionConflictException(tier);
                });
    }
}
