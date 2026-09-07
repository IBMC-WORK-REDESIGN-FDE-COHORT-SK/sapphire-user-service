package com.sapphire.userservice.controller;

import com.sapphire.userservice.dto.ActivePromotionResponse;
import com.sapphire.userservice.model.UserTier;
import com.sapphire.userservice.service.PromotionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * {@link WebMvcTest} slice for {@link PromotionController}.
 * Verifies HTTP semantics: status codes, response body shape, header extraction.
 */
@WebMvcTest(PromotionController.class)
class PromotionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PromotionService promotionService;

    private static final String BASE = "/api/v1/promotions";

    // ────────────────────────────────────────────────────────────────────────
    // GET /active
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void getActivePromotion_returns200_whenPromotionExists() throws Exception {
        ActivePromotionResponse response = buildResponse("Summer Offer");
        when(promotionService.getActivePromotion(UserTier.FREE)).thenReturn(Optional.of(response));

        mockMvc.perform(get(BASE + "/active")
                        .header("X-User-Tier", "FREE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Summer Offer"))
                .andExpect(jsonPath("$.ctaLabel").value("Upgrade"))
                .andExpect(jsonPath("$.targetTier").value("FREE"));
    }

    @Test
    void getActivePromotion_returns204_whenNoActivePromotion() throws Exception {
        when(promotionService.getActivePromotion(UserTier.FREE)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE + "/active")
                        .header("X-User-Tier", "FREE")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getActivePromotion_returns204_forPremiumTier() throws Exception {
        when(promotionService.getActivePromotion(UserTier.PREMIUM)).thenReturn(Optional.empty());

        mockMvc.perform(get(BASE + "/active")
                        .header("X-User-Tier", "PREMIUM")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void getActivePromotion_returns400_whenTierHeaderIsInvalid() throws Exception {
        mockMvc.perform(get(BASE + "/active")
                        .header("X-User-Tier", "PLATINUM")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(promotionService);
    }

    @Test
    void getActivePromotion_returns400_whenTierHeaderIsMissing() throws Exception {
        mockMvc.perform(get(BASE + "/active")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /{id}/click
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void recordClick_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(promotionService).recordClick(id, "FREE");

        mockMvc.perform(post(BASE + "/" + id + "/click")
                        .header("X-User-Tier", "FREE"))
                .andExpect(status().isNoContent());

        verify(promotionService).recordClick(id, "FREE");
    }

    @Test
    void recordClick_returns204_evenWhenServiceThrows() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("db error")).when(promotionService).recordClick(any(), any());

        // FR-018: controller swallows the exception
        mockMvc.perform(post(BASE + "/" + id + "/click")
                        .header("X-User-Tier", "FREE"))
                .andExpect(status().isNoContent());
    }

    // ────────────────────────────────────────────────────────────────────────
    // POST /{id}/dismiss
    // ────────────────────────────────────────────────────────────────────────

    @Test
    void recordDismiss_returns204() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(promotionService).recordDismiss(id, "FREE");

        mockMvc.perform(post(BASE + "/" + id + "/dismiss")
                        .header("X-User-Tier", "FREE"))
                .andExpect(status().isNoContent());

        verify(promotionService).recordDismiss(id, "FREE");
    }

    @Test
    void recordDismiss_returns204_evenWhenServiceThrows() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RuntimeException("db error")).when(promotionService).recordDismiss(any(), any());

        mockMvc.perform(post(BASE + "/" + id + "/dismiss")
                        .header("X-User-Tier", "FREE"))
                .andExpect(status().isNoContent());
    }

    // ────────────────────────────────────────────────────────────────────────
    // helpers
    // ────────────────────────────────────────────────────────────────────────

    private ActivePromotionResponse buildResponse(String title) {
        return new ActivePromotionResponse(
                UUID.randomUUID(), title,
                "20% OFF", "Great deal awaits you.",
                "Upgrade", "https://example.com/upgrade",
                "#FFFFFF", "#000000", "FREE",
                Instant.now(), Instant.now().plusSeconds(3600));
    }
}
