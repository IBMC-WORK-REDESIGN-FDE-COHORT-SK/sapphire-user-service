package com.sapphire.userservice.model;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "user_alerts")
public class AlertEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "metric_name", nullable = false)
    private String metricName;

    @Column(name = "metric_type", nullable = false)
    private String metricType;

    @Column(name = "alert_message", nullable = false)
    private String alertMessage;

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

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getMetricName() {
        return metricName;
    }

    public String getMetricType() {
        return metricType;
    }

    public String getAlertMessage() {
        return alertMessage;
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

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
    }

    public void setAlertMessage(String alertMessage) {
        this.alertMessage = alertMessage;
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