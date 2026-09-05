package com.sapphire.userservice.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "users")
@NamedQuery(
    name = "UserEntity.findByEmail",
    query = "SELECT u FROM UserEntity u WHERE u.email = :email"
)
public class UserEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_tier", nullable = false)
    private UserTier tier;

    @Type(JsonBinaryType.class)
    @Column(name = "physical_attributes", columnDefinition = "jsonb")
    private Map<String, Object> physicalAttributes;

    @Type(JsonBinaryType.class)
    @Column(name = "address", columnDefinition = "jsonb")
    private Map<String, Object> address;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> labels;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> annotations;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Object links;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Object tags;

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

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public UserTier getTier() {
        return tier;
    }

    public Map<String, Object> getPhysicalAttributes() {
        return physicalAttributes;
    }

    public Map<String, Object> getAddress() {
        return address;
    }

    public Map<String, Object> getLabels() {
        return labels;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public Map<String, Object> getAnnotations() {
        return annotations;
    }

    public Object getLinks() {
        return links;
    }

    public Object getTags() {
        return tags;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setTier(UserTier tier) {
        this.tier = tier;
    }

    public void setPhysicalAttributes(Map<String, Object> physicalAttributes) {
        this.physicalAttributes = physicalAttributes;
    }

    public void setAddress(Map<String, Object> address) {
        this.address = address;
    }

    public void setLabels(Map<String, Object> labels) {
        this.labels = labels;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public void setAnnotations(Map<String, Object> annotations) {
        this.annotations = annotations;
    }

    public void setLinks(Object links) {
        this.links = links;
    }

    public void setTags(Object tags) {
        this.tags = tags;
    }
}