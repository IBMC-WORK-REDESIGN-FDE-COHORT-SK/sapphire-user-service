package com.sapphire.userservice.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Read-only response DTO returned by GET /api/v1/promotions/active.
 * Immutable by design — no setters.
 */
public class ActivePromotionResponse {

    private final UUID id;
    private final String title;
    private final String badgeLabel;
    private final String bodyText;
    private final String ctaLabel;
    private final String ctaUrl;
    private final String backgroundColour;
    private final String textColour;
    private final String targetTier;
    private final Instant startsAt;
    private final Instant expiresAt;

    public ActivePromotionResponse(
            UUID id,
            String title,
            String badgeLabel,
            String bodyText,
            String ctaLabel,
            String ctaUrl,
            String backgroundColour,
            String textColour,
            String targetTier,
            Instant startsAt,
            Instant expiresAt) {
        this.id = id;
        this.title = title;
        this.badgeLabel = badgeLabel;
        this.bodyText = bodyText;
        this.ctaLabel = ctaLabel;
        this.ctaUrl = ctaUrl;
        this.backgroundColour = backgroundColour;
        this.textColour = textColour;
        this.targetTier = targetTier;
        this.startsAt = startsAt;
        this.expiresAt = expiresAt;
    }

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getBadgeLabel() { return badgeLabel; }
    public String getBodyText() { return bodyText; }
    public String getCtaLabel() { return ctaLabel; }
    public String getCtaUrl() { return ctaUrl; }
    public String getBackgroundColour() { return backgroundColour; }
    public String getTextColour() { return textColour; }
    public String getTargetTier() { return targetTier; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getExpiresAt() { return expiresAt; }
}
