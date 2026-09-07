package com.sapphire.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sapphire.userservice.dto.ActivePromotionResponse;
import com.sapphire.userservice.service.ActivePromotionConflictException;
import com.sapphire.userservice.service.PromotionNotFoundException;
import com.sapphire.userservice.service.PromotionService;
import com.sapphire.userservice.service.PromotionValidationException;
import com.sapphire.userservice.model.UserTier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link WebMvcTest} slice for {@link AdminPromotionController}.
 */
@WebMvcTest(AdminPromotionController.class)
class AdminPromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    private static final String BASE = "/api/v1/admin/promotions";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    // ────────────────────────────────────────────────────────────────────────
    // POST /api/v1/admin/promotions
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void createPromotion_returns201_withCreatedResource() throws Exception {
        ActivePromotionResponse created = buildResponse(UUID.randomUUID(), "Spring Sale");
        when(promotionService.createPromotion(any())).thenReturn(created);

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson("Spring Sale")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring Sale"));
    }

    @Test
    void createPromotion_returns400_onValidationError() throws Exception {
        when(promotionService.createPromotion(any()))
                .thenThrow(new PromotionValidationException("title is required"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson("")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("title is required"));
    }

    @Test
    void createPromotion_returns409_onConflict() throws Exception {
        when(promotionService.createPromotion(any()))
                .thenThrow(new ActivePromotionConflictException(UserTier.FREE));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson("Offer")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("FREE")));
    }

    // ────────────────────────────────────────────────────────────────────────
    // PATCH /api/v1/admin/promotions/{id}
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void updatePromotion_returns200_withUpdatedResource() throws Exception {
        UUID id = UUID.randomUUID();
        ActivePromotionResponse updated = buildResponse(id, "Updated Title");
        when(promotionService.updatePromotion(eq(id), any())).thenReturn(updated);

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void updatePromotion_returns404_whenNotFound() throws Exception {
        UUID id = UUID.randomUUID();
        when(promotionService.updatePromotion(eq(id), any()))
                .thenThrow(new PromotionNotFoundException(id));

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Whatever\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePromotion_returns400_onValidationError() throws Exception {
        UUID id = UUID.randomUUID();
        when(promotionService.updatePromotion(eq(id), any()))
                .thenThrow(new PromotionValidationException("expiresAt must be strictly after startsAt"));

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"T\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("expiresAt")));
    }

    @Test
    void updatePromotion_returns409_onConflict() throws Exception {
        UUID id = UUID.randomUUID();
        when(promotionService.updatePromotion(eq(id), any()))
                .thenThrow(new ActivePromotionConflictException(UserTier.FREE));

        mockMvc.perform(patch(BASE + "/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"active\":true}"))
                .andExpect(status().isConflict());
    }

    // ────────────────────────────────────────────────────────────────────────
    // helpers
    // ────────────────────────────────────────────────────────────────────────

    private ActivePromotionResponse buildResponse(UUID id, String title) {
        return new ActivePromotionResponse(
                id, title,
                "SAVE 20%", "Great deal!",
                "Upgrade", "https://example.com/upgrade",
                "#FFFFFF", "#000000", "FREE",
                Instant.now(), Instant.now().plusSeconds(86400));
    }

    private String validCreateJson(String title) throws Exception {
        return mapper.writeValueAsString(Map.of(
                "title",      title,
                "badgeLabel", "SAVE 20%",
                "ctaLabel",   "Upgrade",
                "ctaUrl",     "https://example.com/upgrade",
                "targetTier", "FREE",
                "startsAt",   Instant.now().toString(),
                "expiresAt",  Instant.now().plusSeconds(3600).toString(),
                "active",     true
        ));
    }
}
