package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UpdateUserRequest;
import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.model.UserSummaryDto;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserService}.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final String USER_EMAIL = "alice@example.com";
    private static final String FULL_NAME = "Alice Smith";

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    // ---- getUserByEmail ----

    @Test
    void getUserByEmail_returnsUser_whenFound() {
        UserEntity user = buildUser(USER_EMAIL, FULL_NAME, UserTier.PREMIUM);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        Optional<UserEntity> result = userService.getUserByEmail(USER_EMAIL);

        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(USER_EMAIL);
        verify(userRepository).findByEmail(USER_EMAIL);
    }

    @Test
    void getUserByEmail_returnsEmpty_whenNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        Optional<UserEntity> result = userService.getUserByEmail(USER_EMAIL);

        assertThat(result).isEmpty();
    }

    // ---- getAllUsersPaginated ----

    @Test
    void getAllUsersPaginated_returnsMappedDtos_whenUsersExist() {
        UserEntity user = buildUser(USER_EMAIL, FULL_NAME, UserTier.PREMIUM);
        Page<UserEntity> entityPage = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(entityPage);

        Page<UserSummaryDto> result = userService.getAllUsersPaginated(0, 20);

        assertThat(result.getContent()).hasSize(1);
        UserSummaryDto dto = result.getContent().get(0);
        assertThat(dto.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(dto.getFullName()).isEqualTo(FULL_NAME);
        assertThat(dto.getTier()).isEqualTo("PREMIUM");
        verify(userRepository).findAll(PageRequest.of(0, 20));
    }

    @Test
    void getAllUsersPaginated_returnsEmptyPage_whenNoUsersExist() {
        Page<UserEntity> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(emptyPage);

        Page<UserSummaryDto> result = userService.getAllUsersPaginated(0, 20);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void getAllUsersPaginated_handlesNullTier_gracefully() {
        UserEntity user = buildUser(USER_EMAIL, FULL_NAME, null);
        Page<UserEntity> entityPage = new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1);
        when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(entityPage);

        Page<UserSummaryDto> result = userService.getAllUsersPaginated(0, 20);

        assertThat(result.getContent().get(0).getTier()).isNull();
    }

    // ---- updateUser ----

    @Test
    void updateUser_updatesFullName_whenFullNameProvided() {
        UserEntity user = buildUser(USER_EMAIL, "Old Name", UserTier.FREE);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setFullName(FULL_NAME);

        UserEntity result = userService.updateUser(USER_EMAIL, req);

        assertThat(result.getFullName()).isEqualTo(FULL_NAME);
        verify(userRepository).save(user);
    }

    @Test
    void updateUser_updatesTier_whenValidTierProvided() {
        UserEntity user = buildUser(USER_EMAIL, FULL_NAME, UserTier.FREE);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();
        req.setTier("PREMIUM");

        userService.updateUser(USER_EMAIL, req);

        assertThat(user.getTier()).isEqualTo(UserTier.PREMIUM);
    }

    @Test
    void updateUser_throwsException_whenInvalidTierProvided() {
        UserEntity user = buildUser(USER_EMAIL, FULL_NAME, UserTier.FREE);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));

        UpdateUserRequest req = new UpdateUserRequest();
        req.setTier("GOLD");

        assertThatThrownBy(() -> userService.updateUser(USER_EMAIL, req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid tier value");
    }

    @Test
    void updateUser_throwsException_whenUserNotFound() {
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(USER_EMAIL, new UpdateUserRequest()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void updateUser_skipsFieldUpdate_whenFieldIsNull() {
        UserEntity user = buildUser(USER_EMAIL, FULL_NAME, UserTier.PREMIUM);
        when(userRepository.findByEmail(USER_EMAIL)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        UpdateUserRequest req = new UpdateUserRequest();

        userService.updateUser(USER_EMAIL, req);

        assertThat(user.getFullName()).isEqualTo(FULL_NAME);
        assertThat(user.getTier()).isEqualTo(UserTier.PREMIUM);
    }

    // ---- helpers ----

    private UserEntity buildUser(String email, String fullName, UserTier tier) {
        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFullName(fullName);
        user.setTier(tier);
        return user;
    }
}
