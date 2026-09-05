package com.sapphire.userservice.repository;

import com.sapphire.userservice.model.AlertEntity;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertRepository extends JpaRepository<AlertEntity, UUID> {

    List<AlertEntity> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
