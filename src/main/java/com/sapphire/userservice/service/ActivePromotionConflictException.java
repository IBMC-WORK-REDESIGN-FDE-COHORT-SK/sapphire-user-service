package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UserTier;

/**
 * Thrown when a create/activate request would result in more than one active
 * promotion for the same target tier (FR-009).
 */
public class ActivePromotionConflictException extends RuntimeException {

    public ActivePromotionConflictException(UserTier tier) {
        super("An active promotion already exists for tier: " + tier.name());
    }
}
