package com.sapphire.userservice.service;

import com.sapphire.userservice.model.SubscriptionEntity;
import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.repository.SubscriptionRepository;
import com.sapphire.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class SubscriptionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionService.class);

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SubscriptionEntity subscribe(String userEmail, UUID serviceId, String endDate) {
        LOGGER.info("Creating subscription for user {} to service {}", userEmail, serviceId);

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail));

        UUID userId = user.getId();

        // Check if already subscribed
        var existingSubscription = subscriptionRepository
                .findByUserIdAndPartnerServiceIdAndIsActive(userId, serviceId, true);

        if (existingSubscription.isPresent()) {
            throw new IllegalArgumentException("User is already subscribed to this service");
        }

        SubscriptionEntity subscription = new SubscriptionEntity();
        subscription.setUserId(userId);
        subscription.setPartnerServiceId(serviceId);
        subscription.setIsActive(true);

        // Set association context with endDate
        Map<String, Object> context = new HashMap<>();
        context.put("endDate", endDate);
        subscription.setAssociationContext(context);

        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionEntity> listUserSubscriptions(String userEmail) {
        LOGGER.info("Fetching subscriptions for user {}", userEmail);

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail));

        return subscriptionRepository.findByUserIdAndIsActive(user.getId(), true);
    }

    @Transactional
    public void unsubscribe(String userEmail, UUID serviceId) {
        LOGGER.info("Unsubscribing user {} from service {}", userEmail, serviceId);

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail));

        UUID userId = user.getId();

        SubscriptionEntity subscription = subscriptionRepository
                .findByUserIdAndPartnerServiceIdAndIsActive(userId, serviceId, true)
                .orElseThrow(() -> new IllegalArgumentException("Active subscription not found"));

        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);

        LOGGER.info("Successfully unsubscribed user {} from service {}", userEmail, serviceId);
    }
}

// Made with Bob
