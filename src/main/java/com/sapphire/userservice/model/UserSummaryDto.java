package com.sapphire.userservice.model;

import java.util.UUID;

/**
 * Lightweight projection of {@link UserEntity} returned by the paginated
 * {@code GET /api/v1/users} endpoint.
 */
public class UserSummaryDto {

    private UUID id;
    private String email;
    private String fullName;
    private String tier;

    public UserSummaryDto() {}

    public UserSummaryDto(UUID id, String email, String fullName, String tier) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.tier = tier;
    }

    public UUID getId() { return id; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getTier() { return tier; }

    public void setId(UUID id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setTier(String tier) { this.tier = tier; }
}
