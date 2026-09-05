package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UserRecommendationEntity;
import com.sapphire.userservice.model.UserRecommendationRequest;
import com.sapphire.userservice.model.UserRecommendationResponse;
import com.sapphire.userservice.repository.UserRecommendationRepository;
import com.sapphire.userservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UserRecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRecommendationService.class);

    private final UserRecommendationRepository recommendationRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final String partnerServiceBaseUrl;

    public UserRecommendationService(
            UserRecommendationRepository recommendationRepository,
            UserRepository userRepository,
            @Value("${partner.service.base-url}") String partnerServiceBaseUrl
    ) {
        this.recommendationRepository = recommendationRepository;
        this.userRepository = userRepository;
        this.partnerServiceBaseUrl = partnerServiceBaseUrl;
        this.restTemplate = new RestTemplate();
    }

    @Transactional
    public UserRecommendationEntity createRecommendation(String userEmail, UserRecommendationRequest request) {
        UUID userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail))
                .getId();

        UserRecommendationEntity entity = new UserRecommendationEntity();
        entity.setId(request.getId());
        entity.setUserId(userId);
        entity.setCreatedAt(request.getCreatedAt());
        entity.setUpdatedAt(request.getUpdatedAt());

        if (request.getMetadata() != null) {
            entity.setLabels(request.getMetadata().getLabels());
            entity.setTags(request.getMetadata().getTags());
            entity.setAnnotations(request.getMetadata().getAnnotations());
        }

        if (request.getSpec() != null) {
            UUID partnerServiceId = null;
            Integer relevanceScore = null;
            Map<String, Object> spec = new HashMap<>();

            if (request.getSpec().getRecommendation() != null) {
                Map<String, Object> recommendation = new HashMap<>();
                relevanceScore = request.getSpec().getRecommendation().getRelevanceScore();
                recommendation.put("relevanceScore", relevanceScore);
                recommendation.put("generatedAt", request.getSpec().getRecommendation().getGeneratedAt());
                spec.put("recommendation", recommendation);
            }

            if (request.getSpec().getPartnerService() != null) {
                Map<String, Object> partnerService = new HashMap<>();
                partnerServiceId = request.getSpec().getPartnerService().getServiceId();
                partnerService.put("serviceId", partnerServiceId);
                spec.put("partnerService", partnerService);
            }

            if (partnerServiceId == null) {
                throw new IllegalArgumentException("spec.partnerService.serviceId is required");
            }

            entity.setPartnerServiceId(partnerServiceId);
            entity.setRelevanceScore(relevanceScore);
            entity.setSpec(spec);
        } else {
            throw new IllegalArgumentException("spec is required");
        }

        if (entity.getCreatedAt() == null) {
            entity.setCreatedAt(Instant.now());
        }
        if (entity.getUpdatedAt() == null) {
            entity.setUpdatedAt(entity.getCreatedAt());
        }

        return recommendationRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public List<UserRecommendationResponse> getRecommendationsByUserEmail(String userEmail) {
        UUID userId = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + userEmail))
                .getId();

        List<UserRecommendationEntity> recommendations =
                recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);

        List<UserRecommendationResponse> response = new ArrayList<>();
        for (UserRecommendationEntity entity : recommendations) {
            response.add(mapToEnrichedResponse(entity));
        }

        return response;
    }

    private UserRecommendationResponse mapToEnrichedResponse(UserRecommendationEntity entity) {
        UserRecommendationResponse response = new UserRecommendationResponse();
        response.setId(entity.getId());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        UserRecommendationResponse.Metadata metadata = new UserRecommendationResponse.Metadata();
        metadata.setLabels(entity.getLabels());
        metadata.setAnnotations(entity.getAnnotations());
        metadata.setTags(entity.getTags());
        response.setMetadata(metadata);

        UserRecommendationResponse.Spec spec = new UserRecommendationResponse.Spec();

        UserRecommendationResponse.Recommendation recommendation = new UserRecommendationResponse.Recommendation();
        recommendation.setRelevanceScore(entity.getRelevanceScore());

        Map<String, Object> storedSpec = entity.getSpec();
        if (storedSpec != null) {
            Map<String, Object> storedRecommendation = asMap(storedSpec.get("recommendation"));
            if (storedRecommendation != null) {
                recommendation.setGeneratedAt(storedRecommendation.get("generatedAt"));
                if (recommendation.getRelevanceScore() == null) {
                    recommendation.setRelevanceScore(asInteger(storedRecommendation.get("relevanceScore")));
                }
            }
        }
        spec.setRecommendation(recommendation);

        spec.setPartnerService(fetchAndMapPartnerService(entity.getPartnerServiceId()));
        response.setSpec(spec);
        return response;
    }

    private UserRecommendationResponse.PartnerService fetchAndMapPartnerService(UUID partnerServiceId) {
        UserRecommendationResponse.PartnerService partnerService = new UserRecommendationResponse.PartnerService();
        partnerService.setServiceId(partnerServiceId);

        if (partnerServiceId == null) {
            return partnerService;
        }

        String url = partnerServiceBaseUrl + "/partner-services/services/" + partnerServiceId;
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) {
                return partnerService;
            }

            UUID resolvedId = asUuid(body.get("id"));
            if (resolvedId != null) {
                partnerService.setServiceId(resolvedId);
            }
            partnerService.setStatus(asString(body.get("status")));

            Map<String, Object> spec = asMap(body.get("spec"));
            if (spec != null) {
                partnerService.setServiceCode(asString(spec.get("serviceCode")));
                partnerService.setName(asString(spec.get("name")));
                partnerService.setCategory(asString(spec.get("category")));
                partnerService.setServiceType(asString(spec.get("serviceType")));
                partnerService.setDescription(asString(spec.get("description")));
            }

            Map<String, Object> metadata = asMap(body.get("metadata"));
            if (metadata != null) {
                partnerService.setLabels(asMap(metadata.get("labels")));
                partnerService.setAnnotations(asMap(metadata.get("annotations")));
                partnerService.setTags(metadata.get("tags"));
                partnerService.setLinks(metadata.get("links"));
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to fetch partner service details from {}: {}", url, e.getMessage());
        }

        return partnerService;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> asMap(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return null;
    }

    private UUID asUuid(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? null : value.toString();
    }
}
