package com.sapphire.userservice.model;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class UserRecommendationResponse {

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
        private Object generatedAt;

        public Integer getRelevanceScore() {
            return relevanceScore;
        }

        public void setRelevanceScore(Integer relevanceScore) {
            this.relevanceScore = relevanceScore;
        }

        public Object getGeneratedAt() {
            return generatedAt;
        }

        public void setGeneratedAt(Object generatedAt) {
            this.generatedAt = generatedAt;
        }
    }

    public static class PartnerService {
        private UUID serviceId;
        private String serviceCode;
        private String name;
        private String category;
        private String serviceType;
        private String description;
        private String status;
        private Map<String, Object> labels;
        private Map<String, Object> annotations;
        private Object tags;
        private Object links;

        public UUID getServiceId() {
            return serviceId;
        }

        public void setServiceId(UUID serviceId) {
            this.serviceId = serviceId;
        }

        public String getServiceCode() {
            return serviceCode;
        }

        public void setServiceCode(String serviceCode) {
            this.serviceCode = serviceCode;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public String getServiceType() {
            return serviceType;
        }

        public void setServiceType(String serviceType) {
            this.serviceType = serviceType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Map<String, Object> getLabels() {
            return labels;
        }

        public void setLabels(Map<String, Object> labels) {
            this.labels = labels;
        }

        public Map<String, Object> getAnnotations() {
            return annotations;
        }

        public void setAnnotations(Map<String, Object> annotations) {
            this.annotations = annotations;
        }

        public Object getTags() {
            return tags;
        }

        public void setTags(Object tags) {
            this.tags = tags;
        }

        public Object getLinks() {
            return links;
        }

        public void setLinks(Object links) {
            this.links = links;
        }
    }
}
