package com.sapphire.userservice.repository;

import com.sapphire.userservice.model.UserRecommendationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserRecommendationRepository extends JpaRepository<UserRecommendationEntity, UUID> {

    List<UserRecommendationEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
