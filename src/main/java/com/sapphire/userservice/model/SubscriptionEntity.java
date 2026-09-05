package com.sapphire.userservice.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_partner_services_subscriptions")
public class SubscriptionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "partner_service_id", nullable = false)
    private UUID partnerServiceId;

    @Type(JsonBinaryType.class)
    @Column(name = "association_context", columnDefinition = "jsonb")
    private Map<String, Object> associationContext;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getPartnerServiceId() {
        return partnerServiceId;
    }

    public void setPartnerServiceId(UUID partnerServiceId) {
        this.partnerServiceId = partnerServiceId;
    }

    public Map<String, Object> getAssociationContext() {
        return associationContext;
    }

    public void setAssociationContext(Map<String, Object> associationContext) {
        this.associationContext = associationContext;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}

// Made with Bob
