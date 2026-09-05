package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.model.UserRecommendationEntity;
import com.sapphire.userservice.model.UserRecommendationRequest;
import com.sapphire.userservice.model.UserRecommendationResponse;
import com.sapphire.userservice.repository.UserRecommendationRepository;
import com.sapphire.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserRecommendationService}.
 * RestTemplate is injected via reflection to avoid external HTTP calls.
 */
@ExtendWith(MockitoExtension.class)
class UserRecommendationServiceTest {

    private static final String USER_EMAIL = "eve@example.com";
    private static final String BASE_URL = "http://partner-service";

    @Mock
    private UserRecommendationRepository recommendationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    private UserRecommendationService userRecommendationService;

    @BeforeEach
    void setUp() {
        userRecommendationService = new UserRecommendationService(
                recommendationRepository, userRepository, BASE_URL);
        ReflectionTestUtils.setField(userRecommendationService, "restTemplate", restTemplate);
    }

    // ---- createRecommendation ----

    @Test
    void createRecommendation_savesEntity_whenValidRequest() {
        UUID userId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        UserRecommendationRequest request = buildRequest(serviceId, 85);
        UserRecommendationEntity saved = new UserRecommendationEntity();
        saved.setUserId(userId);
        saved.setPartnerServiceId(serviceId);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(recommendationRepository.save(any(UserRecommendationEntity.class))).thenReturn(saved);

        UserRecommendationEntity result = userRecommendationService.createRecommendation(USER_EMAIL, request);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getPartnerServiceId()).isEqualTo(serviceId);
        verify(recommendationRepository).save(any(UserRecommendationEntity.class));
    }

    @Test
    void createRecommendation_throwsException_whenSpecIsNull() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        UserRecommendationRequest request = new UserRecommendationRequest();
        // spec is null

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userRecommendationService.createRecommendation(USER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spec is required");
    }

    @Test
    void createRecommendation_throwsException_whenPartnerServiceIdIsNull() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        UserRecommendationRequest request = buildRequestWithNullServiceId();

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userRecommendationService.createRecommendation(USER_EMAIL, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("spec.partnerService.serviceId is required");
    }

    @Test
    void createRecommendation_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                userRecommendationService.createRecommendation(USER_EMAIL, buildRequest(UUID.randomUUID(), 70)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- getRecommendationsByUserEmail ----

    @Test
    void getRecommendationsByUserEmail_returnsEnrichedResponses() {
        UUID userId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        UserRecommendationEntity entity = new UserRecommendationEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setPartnerServiceId(serviceId);
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(recommendationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(entity));
        when(restTemplate.getForEntity(anyString(), eq(Map.class))).thenReturn(ResponseEntity.of(Optional.empty()));

        List<UserRecommendationResponse> result =
                userRecommendationService.getRecommendationsByUserEmail(USER_EMAIL);

        assertThat(result).hasSize(1);
        verify(recommendationRepository).findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void getRecommendationsByUserEmail_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userRecommendationService.getRecommendationsByUserEmail(USER_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- helpers ----

    private UserEntity buildUser(UUID id, String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private UserRecommendationRequest buildRequest(UUID serviceId, int score) {
        UserRecommendationRequest request = new UserRecommendationRequest();
        request.setId(UUID.randomUUID());
        request.setCreatedAt(Instant.now());
        request.setUpdatedAt(Instant.now());

        UserRecommendationRequest.Spec spec = new UserRecommendationRequest.Spec();

        UserRecommendationRequest.Recommendation recommendation = new UserRecommendationRequest.Recommendation();
        recommendation.setRelevanceScore(score);
        recommendation.setGeneratedAt(Instant.now());
        spec.setRecommendation(recommendation);

        UserRecommendationRequest.PartnerService partnerService = new UserRecommendationRequest.PartnerService();
        partnerService.setServiceId(serviceId);
        spec.setPartnerService(partnerService);

        request.setSpec(spec);
        return request;
    }

    private UserRecommendationRequest buildRequestWithNullServiceId() {
        UserRecommendationRequest request = new UserRecommendationRequest();
        UserRecommendationRequest.Spec spec = new UserRecommendationRequest.Spec();
        // partnerService is null — no serviceId
        request.setSpec(spec);
        return request;
    }
}
