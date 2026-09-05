package com.sapphire.userservice.controller;

import com.sapphire.userservice.model.SubscriptionEntity;
import com.sapphire.userservice.model.SubscriptionRequest;
import com.sapphire.userservice.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class SubscriptionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionController.class);

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{userEmail}/subscriptions")
    public ResponseEntity<?> subscribe(
            @PathVariable String userEmail,
            @RequestBody SubscriptionRequest request) {

        LOGGER.info("Subscribe request for user {} to service {}", userEmail, request.getServiceId());

        try {
            SubscriptionEntity subscription = subscriptionService.subscribe(
                    userEmail,
                    request.getServiceId(),
                    request.getEndDate()
            );
            return ResponseEntity.ok(subscription);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Subscription failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{userEmail}/subscriptions")
    public ResponseEntity<?> listSubscriptions(@PathVariable String userEmail) {
        LOGGER.info("Fetching subscriptions for user {}", userEmail);

        try {
            List<SubscriptionEntity> subscriptions = subscriptionService.listUserSubscriptions(userEmail);
            return ResponseEntity.ok(subscriptions);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to fetch subscriptions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{userEmail}/subscriptions/{serviceId}")
    public ResponseEntity<?> unsubscribe(
            @PathVariable String userEmail,
            @PathVariable UUID serviceId) {

        LOGGER.info("Unsubscribe request for user {} from service {}", userEmail, serviceId);

        try {
            subscriptionService.unsubscribe(userEmail, serviceId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Unsubscribe failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

// Made with Bob
