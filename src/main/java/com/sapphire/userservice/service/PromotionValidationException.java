package com.sapphire.userservice.service;

/**
 * Thrown when a promotion create/update request violates business rules
 * (e.g. invalid window, field length exceeded, conflicting active promotion).
 */
public class PromotionValidationException extends RuntimeException {

    public PromotionValidationException(String message) {
        super(message);
    }
}
