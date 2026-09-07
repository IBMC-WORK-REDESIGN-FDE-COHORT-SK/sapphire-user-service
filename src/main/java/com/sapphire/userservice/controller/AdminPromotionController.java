package com.sapphire.userservice.controller;

import com.sapphire.userservice.dto.ActivePromotionResponse;
import com.sapphire.userservice.dto.CreatePromotionRequest;
import com.sapphire.userservice.dto.UpdatePromotionRequest;
import com.sapphire.userservice.service.ActivePromotionConflictException;
import com.sapphire.userservice.service.PromotionNotFoundException;
import com.sapphire.userservice.service.PromotionService;
import com.sapphire.userservice.service.PromotionValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Admin endpoint for promotion CRUD. In production this would be protected
 * at the API-gateway / BFF layer. No Spring Security in this service
 * (env-finding #3).
 */
@RestController
@RequestMapping("/api/v1/admin/promotions")
public class AdminPromotionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminPromotionController.class);

    private final PromotionService promotionService;

    public AdminPromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    /**
     * POST /api/v1/admin/promotions
     *
     * Creates a new promotion. Returns 201 with the created resource.
     */
    @PostMapping
    public ResponseEntity<?> createPromotion(@RequestBody CreatePromotionRequest req) {
        LOGGER.info("POST /admin/promotions title='{}' tier={}", req.getTitle(), req.getTargetTier());
        try {
            ActivePromotionResponse created = promotionService.createPromotion(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (PromotionValidationException ex) {
            LOGGER.warn("Validation error creating promotion: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (ActivePromotionConflictException ex) {
            LOGGER.warn("Conflict creating promotion: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }

    /**
     * PATCH /api/v1/admin/promotions/{id}
     *
     * Partially updates an existing promotion. Returns 200 with the updated resource.
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> updatePromotion(
            @PathVariable UUID id,
            @RequestBody UpdatePromotionRequest req) {
        LOGGER.info("PATCH /admin/promotions/{}", id);
        try {
            ActivePromotionResponse updated = promotionService.updatePromotion(id, req);
            return ResponseEntity.ok(updated);
        } catch (PromotionNotFoundException ex) {
            LOGGER.warn("Promotion not found id={}", id);
            return ResponseEntity.notFound().build();
        } catch (PromotionValidationException ex) {
            LOGGER.warn("Validation error updating promotion id={}: {}", id, ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (ActivePromotionConflictException ex) {
            LOGGER.warn("Conflict updating promotion id={}: {}", id, ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        }
    }
}
