package com.sapphire.userservice.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class UserRecommendationRequest {

    private UUID id;
    private Instant createdAt;
    private Instant updatedAt;
    private Metadata metadata;
    private Spec spec;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public Spec getSpec() {
        return spec;
    }

    public void setSpec(Spec spec) {
        this.spec = spec;
    }

    public static class Metadata {
        private Map<String, Object> labels;
        private Object tags;
        private Map<String, Object> annotations;

        public Map<String, Object> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, Object> labels) {
            this.labels = labels;
        }

        public Object getTags() {
            return tags;
        }

        public void setTags(Object tags) {
            this.tags = tags;
        }

        public Map<String, Object> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Map<String, Object> annotations) {
            this.annotations = annotations;
        }
    }

    public static class Spec {
        private Recommendation recommendation;
        private PartnerService partnerService;

        public Recommendation getRecommendation() {
            return recommendation;
        }

        public void setRecommendation(Recommendation recommendation) {
            this.recommendation = recommendation;
        }

        public PartnerService getPartnerService() {
            return partnerService;
        }

        public void setPartnerService(PartnerService partnerService) {
            this.partnerService = partnerService;
        }
    }

    public static class Recommendation {
        private Integer relevanceScore;
        private Instant generatedAt;

        public Integer getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Integer relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public Instant getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(Instant generatedAt) {
            this.generatedAt = generatedAt;
        }
    }

    public static class PartnerService {
        private UUID serviceId;

        public UUID getServiceId() {
            return serviceId;
        }

        public void setServiceId(UUID serviceId) {
            this.serviceId = serviceId;
        }
    }
}
