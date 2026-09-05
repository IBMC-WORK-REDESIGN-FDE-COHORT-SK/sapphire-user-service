package com.sapphire.userservice.service;

import com.sapphire.userservice.model.AlertEntity;
import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.repository.AlertRepository;
import com.sapphire.userservice.repository.UserRepository;
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
 * Unit tests for {@link AlertService}.
 */
@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

    private static final String USER_EMAIL = "carol@example.com";
    private static final String METRIC_NAME = "steps";
    private static final String METRIC_TYPE = "activity";
    private static final String ALERT_MSG = "Step count exceeded threshold";

    @Mock
    private AlertRepository alertRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AlertService alertService;

    // ---- createAlert ----

    @Test
    void createAlert_setsUserId_andSavesAlert() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        AlertEntity alert = buildAlert(METRIC_NAME, METRIC_TYPE, ALERT_MSG);
        AlertEntity saved = buildAlert(METRIC_NAME, METRIC_TYPE, ALERT_MSG);
        saved.setUserId(userId);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(alertRepository.save(any(AlertEntity.class))).thenReturn(saved);

        AlertEntity result = alertService.createAlert(alert, USER_EMAIL);

        assertThat(result.getUserId()).isEqualTo(userId);
        verify(alertRepository).save(alert);
    }

    @Test
    void createAlert_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.createAlert(new AlertEntity(), USER_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- getAlertsByUserEmail ----

    @Test
    void getAlertsByUserEmail_returnsAlerts() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        AlertEntity alert = buildAlert(METRIC_NAME, METRIC_TYPE, ALERT_MSG);
        alert.setUserId(userId);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(alertRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(alert));

        List<AlertEntity> result = alertService.getAlertsByUserEmail(USER_EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMetricName()).isEqualTo(METRIC_NAME);
    }

    @Test
    void getAlertsByUserEmail_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> alertService.getAlertsByUserEmail(USER_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- helpers ----

    private UserEntity buildUser(UUID id, String email) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        org.springframework.test.util.ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private AlertEntity buildAlert(String metricName, String metricType, String message) {
        AlertEntity alert = new AlertEntity();
        alert.setMetricName(metricName);
        alert.setMetricType(metricType);
        alert.setAlertMessage(message);
        return alert;
    }
}
