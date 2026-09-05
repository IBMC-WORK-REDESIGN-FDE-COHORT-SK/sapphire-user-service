package com.sapphire.userservice.controller;

import com.sapphire.userservice.model.AlertEntity;
import com.sapphire.userservice.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/alerts")
public class AlertController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AlertController.class);

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @PostMapping
    public ResponseEntity<?> createAlert(@RequestBody AlertEntity alert, @RequestParam String userEmail) {
        LOGGER.info("Creating alert for user {}", userEmail);

        try {
            AlertEntity saved = alertService.createAlert(alert, userEmail);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to create alert: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getAlerts(@RequestParam String userEmail) {
        LOGGER.info("Fetching alerts for user {}", userEmail);

        try {
            return ResponseEntity.ok(alertService.getAlertsByUserEmail(userEmail));
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Failed to fetch alerts: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}