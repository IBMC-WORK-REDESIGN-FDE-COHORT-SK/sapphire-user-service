package com.sapphire.userservice.service;

import java.util.UUID;

/**
 * Thrown when a promotion lookup by ID returns no result.
 */
public class PromotionNotFoundException extends RuntimeException {

    public PromotionNotFoundException(UUID id) {
        super("Promotion not found: " + id);
    }
}
