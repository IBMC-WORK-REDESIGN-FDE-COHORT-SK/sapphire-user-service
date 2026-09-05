package com.sapphire.userservice.repository;

import com.sapphire.userservice.model.WellnessSummaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WellnessSummaryRepository extends JpaRepository<WellnessSummaryEntity, UUID> {

    List<WellnessSummaryEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}