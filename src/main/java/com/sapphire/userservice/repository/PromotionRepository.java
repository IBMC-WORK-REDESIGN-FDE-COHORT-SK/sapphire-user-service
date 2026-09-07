package com.sapphire.userservice.repository;

import com.sapphire.userservice.model.PromotionEntity;
import com.sapphire.userservice.model.UserTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, UUID> {

    /**
     * Returns the single active promotion for a given tier that is currently
     * within its validity window. Per FR-005, startsAt == expiresAt is never
     * served (the DB CHECK constraint also prevents such records).
     */
    @Query("""
            SELECT p FROM PromotionEntity p
            WHERE p.active = true
              AND p.targetTier = :tier
              AND p.startsAt <= :now
              AND p.expiresAt > :now
            """)
    Optional<PromotionEntity> findActiveForTier(
            @Param("tier") UserTier tier,
            @Param("now") Instant now);

    /**
     * Used by PromotionValidationService to detect a conflicting active
     * promotion before creating or activating a new one.
     */
    @Query("""
            SELECT p FROM PromotionEntity p
            WHERE p.active = true
              AND p.targetTier = :tier
              AND p.id <> :excludeId
            """)
    Optional<PromotionEntity> findOtherActiveForTier(
            @Param("tier") UserTier tier,
            @Param("excludeId") UUID excludeId);
}
