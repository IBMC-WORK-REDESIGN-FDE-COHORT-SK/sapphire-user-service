package com.sapphire.userservice.controller;

import com.sapphire.userservice.model.UserRecommendationEntity;
import com.sapphire.userservice.model.UserRecommendationRequest;
import com.sapphire.userservice.service.UserRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users/recommendations")
public class UserRecommendationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRecommendationController.class);

    private final UserRecommendationService recommendationService;

    public UserRecommendationController(UserRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public ResponseEntity<?> createRecommendation(
            @RequestBody UserRecommendationRequest request,
            @RequestParam String userEmail
    ) {
        LOGGER.info("Creating recommendation for user {}", userEmail);

        try {
            UserRecommendationEntity saved = recommendationService.createRecommendation(userEmail, request);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to create recommendation: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getRecommendations(@RequestParam String userEmail) {
        LOGGER.info("Fetching recommendations for user {}", userEmail);

        try {
            return ResponseEntity.ok(recommendationService.getRecommendationsByUserEmail(userEmail));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to fetch recommendations: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
