package com.sapphire.userservice.controller;

import com.sapphire.userservice.dto.ActivePromotionResponse;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.service.PromotionService;
import com.sapphire.userservice.service.PromotionValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Public endpoint consumed by the Sapphire FitConnect frontend.
 * Tier is passed by the BFF as a trusted internal header — no Spring
 * Security present in this service (see env-finding #3).
 */
@RestController
@RequestMapping("/api/v1/promotions")
public class PromotionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionController.class);

    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * GET /api/v1/promotions/active
     *
     * Returns the currently active promotion for the requesting user's tier.
     * Returns 204 No Content when no promotion is active (FR-004).
     *
     * @param userTier  resolved tier forwarded by the BFF (e.g. "FREE")
     */
    @GetMapping("/active")
    public ResponseEntity<ActivePromotionResponse> getActivePromotion(
            @RequestHeader("X-User-Tier") String userTier) {

        LOGGER.info("GET /promotions/active tier={}", userTier);

        UserTier tier;
        try {
            tier = UserTier.valueOf(userTier.toUpperCase());
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Invalid tier header value: {}", userTier);
            return ResponseEntity.badRequest().build();
        }

        Optional<ActivePromotionResponse> promotion = promotionService.getActivePromotion(tier);
        return promotion
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    /**
     * POST /api/v1/promotions/{id}/click
     *
     * Records a CTA click. Failures are non-blocking (FR-018).
     */
    @PostMapping("/{id}/click")
    public ResponseEntity<Void> recordClick(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Tier", defaultValue = "FREE") String userTier) {

        LOGGER.info("POST /promotions/{}/click tier={}", id, userTier);
        try {
            promotionService.recordClick(id, userTier.toUpperCase());
        } catch (Exception ex) {
            LOGGER.warn("Failed to record click for promotion={}: {}", id, ex.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/v1/promotions/{id}/dismiss
     *
     * Records a dismissal. Failures are non-blocking (FR-018).
     */
    @PostMapping("/{id}/dismiss")
    public ResponseEntity<Void> recordDismiss(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Tier", defaultValue = "FREE") String userTier) {

        LOGGER.info("POST /promotions/{}/dismiss tier={}", id, userTier);
        try {
            promotionService.recordDismiss(id, userTier.toUpperCase());
        } catch (Exception ex) {
            LOGGER.warn("Failed to record dismiss for promotion={}: {}", id, ex.getMessage());
        }
        return ResponseEntity.noContent().build();
    }
}
