package com.sapphire.userservice.repository;

import com.sapphire.userservice.model.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, UUID> {

    List<SubscriptionEntity> findByUserIdAndIsActive(UUID userId, Boolean isActive);

    Optional<SubscriptionEntity> findByUserIdAndPartnerServiceIdAndIsActive(UUID userId, UUID partnerServiceId, Boolean isActive);
}

// Made with Bob
