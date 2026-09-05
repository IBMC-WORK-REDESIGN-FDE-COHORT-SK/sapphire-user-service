package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UpdateUserRequest;
import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.model.UserSummaryDto;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.repository.UserRepository;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public Optional<UserEntity> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Page<UserSummaryDto> getAllUsersPaginated(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(u -> new UserSummaryDto(
                        u.getId(),
                        u.getEmail(),
                        u.getFullName(),
                        u.getTier() != null ? u.getTier().name() : null
                ));
    }

    @Transactional
    public UserEntity updateUser(String email, UpdateUserRequest req) {
        UserEntity user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found for email: " + email));

        if (req.getFullName() != null) {
            user.setFullName(req.getFullName());
        }

        if (req.getTier() != null) {
            try {
                user.setTier(UserTier.valueOf(req.getTier().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid tier value: " + req.getTier());
            }
        }

        if (req.getAddress() != null) {
            user.setAddress(req.getAddress());
        }

        if (req.getPhysicalAttributes() != null) {
            user.setPhysicalAttributes(req.getPhysicalAttributes());
        }

        return userRepository.save(user);
    }
}