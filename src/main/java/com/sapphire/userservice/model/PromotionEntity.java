package com.sapphire.userservice.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "promotions")
public class PromotionEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Column(name = "badge_label", length = 20)
    private String badgeLabel;

    @Column(name = "body_text", columnDefinition = "TEXT")
    private String bodyText;

    @Column(name = "cta_label", nullable = false, length = 30)
    private String ctaLabel;

    @Column(name = "cta_url", nullable = false, length = 2048)
    private String ctaUrl;

    @Column(name = "background_colour", nullable = false, length = 7)
    private String backgroundColour = "#FFFFFF";

    @Column(name = "text_colour", nullable = false, length = 7)
    private String textColour = "#000000";

    @Enumerated(EnumType.STRING)
    @Column(name = "target_tier", nullable = false, length = 20)
    private UserTier targetTier = UserTier.FREE;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "impression_count", nullable = false)
    private long impressionCount = 0L;

    @Column(name = "click_count", nullable = false)
    private long clickCount = 0L;

    @Column(name = "dismiss_count", nullable = false)
    private long dismissCount = 0L;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        updatedAt = Instant.now();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getTitle() { return title; }
    public String getBadgeLabel() { return badgeLabel; }
    public String getBodyText() { return bodyText; }
    public String getCtaLabel() { return ctaLabel; }
    public String getCtaUrl() { return ctaUrl; }
    public String getBackgroundColour() { return backgroundColour; }
    public String getTextColour() { return textColour; }
    public UserTier getTargetTier() { return targetTier; }
    public Instant getStartsAt() { return startsAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isActive() { return active; }
    public long getImpressionCount() { return impressionCount; }
    public long getClickCount() { return clickCount; }
    public long getDismissCount() { return dismissCount; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setTitle(String title) { this.title = title; }
    public void setBadgeLabel(String badgeLabel) { this.badgeLabel = badgeLabel; }
    public void setBodyText(String bodyText) { this.bodyText = bodyText; }
    public void setCtaLabel(String ctaLabel) { this.ctaLabel = ctaLabel; }
    public void setCtaUrl(String ctaUrl) { this.ctaUrl = ctaUrl; }
    public void setBackgroundColour(String backgroundColour) { this.backgroundColour = backgroundColour; }
    public void setTextColour(String textColour) { this.textColour = textColour; }
    public void setTargetTier(UserTier targetTier) { this.targetTier = targetTier; }
    public void setStartsAt(Instant startsAt) { this.startsAt = startsAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
    public void setActive(boolean active) { this.active = active; }
    public void setImpressionCount(long impressionCount) { this.impressionCount = impressionCount; }
    public void setClickCount(long clickCount) { this.clickCount = clickCount; }
    public void setDismissCount(long dismissCount) { this.dismissCount = dismissCount; }
}
