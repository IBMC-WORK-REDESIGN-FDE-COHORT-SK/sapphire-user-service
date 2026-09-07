package com.sapphire.userservice.dto;

import java.time.Instant;

/**
 * Request body for POST /api/v1/admin/promotions.
 */
public class CreatePromotionRequest {

    private String title;
    private String badgeLabel;
    private String bodyText;
    private String ctaLabel;
    private String ctaUrl;
    private String backgroundColour;
    private String textColour;
    private String targetTier;
    private Instant startsAt;
    private Instant expiresAt;
    private boolean active = true;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBadgeLabel() { return badgeLabel; }
    public void setBadgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; }

    public String getBodyText() { return bodyText; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }

    public String getCtaLabel() { return ctaLabel; }
    public void setCtaLabel(String ctaLabel) { this.ctaLabel = ctaLabel; }

    public String getCtaUrl() { return ctaUrl; }
    public void setCtaUrl(String ctaUrl) { this.ctaUrl = ctaUrl; }

    public String getBackgroundColour() { return backgroundColour; }
    public void setBackgroundColour(String backgroundColour) { this.backgroundColour = backgroundColour; }

    public String getTextColour() { return textColour; }
    public void setTextColour(String textColour) { this.textColour = textColour; }

    public String getTargetTier() { return targetTier; }
    public void setTargetTier(String targetTier) { this.targetTier = targetTier; }

    public Instant getStartsAt() { return startsAt; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
