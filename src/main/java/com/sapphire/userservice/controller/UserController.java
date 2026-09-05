package com.sapphire.userservice.controller;

import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.model.UserSummaryDto;
import com.sapphire.userservice.service.UserService;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.sapphire.userservice.model.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<UserSummaryDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        LOGGER.info("Fetching all users page={} size={}", page, size);
        return ResponseEntity.ok(userService.getAllUsersPaginated(page, size));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        LOGGER.info("Fetching user by email {}", email);

        Optional<UserEntity> userOpt = userService.getUserByEmail(email);
        if (userOpt.isEmpty()) {
            LOGGER.warn("User not found for email {}", email);
            return ResponseEntity.notFound().build();
        }

        UserEntity u = userOpt.get();

        Map<String, Object> response = Map.of(
            "metadata", Map.of(
                "name", u.getEmail(),
                "labels", u.getLabels(),
                "annotations", u.getAnnotations()
            ),
            "spec", Map.of(
                "email", u.getEmail(),
                "fullName", u.getFullName(),
                "tier", u.getTier().name().toLowerCase(),
                "physicalAttributes", u.getPhysicalAttributes(),
                "address", u.getAddress(),
                "tags", u.getTags(),
                "links", u.getLinks()
            )
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{email}")
    public ResponseEntity<?> updateUser(
            @PathVariable String email,
            @RequestBody UpdateUserRequest req) {

        LOGGER.info("Updating user {}", email);

        try {
            UserEntity updated = userService.updateUser(email, req);

            Map<String, Object> response = Map.of(
                "metadata", Map.of(
                    "name", updated.getEmail(),
                    "labels", updated.getLabels(),
                    "annotations", updated.getAnnotations()
                ),
                "spec", Map.of(
                    "email", updated.getEmail(),
                    "fullName", updated.getFullName(),
                    "tier", updated.getTier().name().toLowerCase(),
                    "physicalAttributes", updated.getPhysicalAttributes(),
                    "address", updated.getAddress(),
                    "tags", updated.getTags(),
                    "links", updated.getLinks()
                )
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            LOGGER.warn("Update failed for {}: {}", email, e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
