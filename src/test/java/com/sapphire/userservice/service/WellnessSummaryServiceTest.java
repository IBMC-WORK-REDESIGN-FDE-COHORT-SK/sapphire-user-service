package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.model.WellnessSummaryEntity;
import com.sapphire.userservice.repository.UserRepository;
import com.sapphire.userservice.repository.WellnessSummaryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link WellnessSummaryService}.
 */
@ExtendWith(MockitoExtension.class)
class WellnessSummaryServiceTest {

    private static final String USER_EMAIL = "dave@example.com";
    private static final String PROFILE_SUMMARY = "Active lifestyle, normal BP.";
    private static final String DATA_SUMMARY = "7-day average: 8500 steps.";

    @Mock
    private WellnessSummaryRepository wellnessSummaryRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WellnessSummaryService wellnessSummaryService;

    // ---- saveSummary ----

    @Test
    void saveSummary_savesEntity_whenUserFound() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        WellnessSummaryEntity saved = new WellnessSummaryEntity();
        saved.setUserId(userId);
        saved.setProfileSummary(PROFILE_SUMMARY);
        saved.setDataSummary(DATA_SUMMARY);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(wellnessSummaryRepository.save(any(WellnessSummaryEntity.class))).thenReturn(saved);

        WellnessSummaryEntity result = wellnessSummaryService.saveSummary(USER_EMAIL, PROFILE_SUMMARY, DATA_SUMMARY);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getProfileSummary()).isEqualTo(PROFILE_SUMMARY);
        verify(wellnessSummaryRepository).save(any(WellnessSummaryEntity.class));
    }

    @Test
    void saveSummary_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> wellnessSummaryService.saveSummary(USER_EMAIL, PROFILE_SUMMARY, DATA_SUMMARY))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- getLatestSummaryByEmail ----

    @Test
    void getLatestSummaryByEmail_returnsLatest_whenSummaryExists() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        WellnessSummaryEntity summary = new WellnessSummaryEntity();
        summary.setUserId(userId);
        summary.setProfileSummary(PROFILE_SUMMARY);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(wellnessSummaryRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(summary));

        Optional<WellnessSummaryEntity> result = wellnessSummaryService.getLatestSummaryByEmail(USER_EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get().getProfileSummary()).isEqualTo(PROFILE_SUMMARY);
    }

    @Test
    void getLatestSummaryByEmail_returnsEmpty_whenNoSummaries() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(wellnessSummaryRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        Optional<WellnessSummaryEntity> result = wellnessSummaryService.getLatestSummaryByEmail(USER_EMAIL);

        assertThat(result).isEmpty();
    }

    @Test
    void getLatestSummaryByEmail_returnsEmpty_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        Optional<WellnessSummaryEntity> result = wellnessSummaryService.getLatestSummaryByEmail(USER_EMAIL);

        assertThat(result).isEmpty();
        verifyNoInteractions(wellnessSummaryRepository);
    }

    // ---- helpers ----

    private UserEntity buildUser(UUID id, String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
