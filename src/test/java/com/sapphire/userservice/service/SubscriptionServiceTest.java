package com.sapphire.userservice.service;

import com.sapphire.userservice.model.SubscriptionEntity;
import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.repository.SubscriptionRepository;
import com.sapphire.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SubscriptionService}.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final String USER_EMAIL = "bob@example.com";
    private static final String END_DATE = "2026-12-31";

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionService subscriptionService;

    // ---- subscribe ----

    @Test
    void subscribe_savesSubscription_whenNotAlreadySubscribed() {
        UUID userId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        SubscriptionEntity saved = newSubscription(userId, serviceId, true);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserIdAndPartnerServiceIdAndIsActive(userId, serviceId, true))
                .thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(SubscriptionEntity.class))).thenReturn(saved);

        SubscriptionEntity result = subscriptionService.subscribe(USER_EMAIL, serviceId, END_DATE);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getIsActive()).isTrue();
        verify(subscriptionRepository).save(any(SubscriptionEntity.class));
    }

    @Test
    void subscribe_throwsException_whenAlreadySubscribed() {
        UUID userId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserIdAndPartnerServiceIdAndIsActive(userId, serviceId, true))
                .thenReturn(Optional.of(newSubscription(userId, serviceId, true)));

        assertThatThrownBy(() -> subscriptionService.subscribe(USER_EMAIL, serviceId, END_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already subscribed");
    }

    @Test
    void subscribe_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.subscribe(USER_EMAIL, UUID.randomUUID(), END_DATE))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- listUserSubscriptions ----

    @Test
    void listUserSubscriptions_returnsActiveSubscriptions() {
        UUID userId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        SubscriptionEntity sub = newSubscription(userId, UUID.randomUUID(), true);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserIdAndIsActive(userId, true)).thenReturn(List.of(sub));

        List<SubscriptionEntity> result = subscriptionService.listUserSubscriptions(USER_EMAIL);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(userId);
    }

    @Test
    void listUserSubscriptions_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.listUserSubscriptions(USER_EMAIL))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ---- unsubscribe ----

    @Test
    void unsubscribe_deactivatesSubscription_whenActive() {
        UUID userId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);
        SubscriptionEntity sub = newSubscription(userId, serviceId, true);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserIdAndPartnerServiceIdAndIsActive(userId, serviceId, true))
                .thenReturn(Optional.of(sub));
        when(subscriptionRepository.save(sub)).thenReturn(sub);

        subscriptionService.unsubscribe(USER_EMAIL, serviceId);

        assertThat(sub.getIsActive()).isFalse();
        verify(subscriptionRepository).save(sub);
    }

    @Test
    void unsubscribe_throwsException_whenSubscriptionNotFound() {
        UUID userId = UUID.randomUUID();
        UUID serviceId = UUID.randomUUID();
        UserEntity user = buildUser(userId, USER_EMAIL);

        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserIdAndPartnerServiceIdAndIsActive(userId, serviceId, true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_EMAIL, serviceId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Active subscription not found");
    }

    @Test
    void unsubscribe_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.unsubscribe(USER_EMAIL, UUID.randomUUID()))
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

    private SubscriptionEntity newSubscription(UUID userId, UUID serviceId, boolean active) {
        SubscriptionEntity sub = new SubscriptionEntity();
        sub.setUserId(userId);
        sub.setPartnerServiceId(serviceId);
        sub.setIsActive(active);
        return sub;
    }
}
