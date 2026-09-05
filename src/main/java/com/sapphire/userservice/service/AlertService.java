package com.sapphire.userservice.service;

import com.sapphire.userservice.model.AlertEntity;
import com.sapphire.userservice.repository.AlertRepository;
import com.sapphire.userservice.repository.UserRepository;
import java.util.UUID;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final UserRepository userRepository;

    public AlertService(AlertRepository alertRepository, UserRepository userRepository) {
        this.alertRepository = alertRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public AlertEntity createAlert(AlertEntity alert, String userEmail) {
        UUID userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail))
            .getId();

        alert.setUserId(userId);

        return alertRepository.save(alert);
    }

    public List<AlertEntity> getAlertsByUserEmail(String userEmail) {
        UUID userId = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail))
            .getId();

        return alertRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
}