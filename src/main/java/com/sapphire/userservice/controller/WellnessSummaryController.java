package com.sapphire.userservice.controller;

import com.sapphire.userservice.model.WellnessSummaryEntity;
import com.sapphire.userservice.service.WellnessSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class WellnessSummaryController {

    public static class WellnessSummaryRequest {
        private String profileSummary;
        private String dataSummary;

        public String getProfileSummary() { return profileSummary; }
        public void setProfileSummary(String profileSummary) { this.profileSummary = profileSummary; }

        public String getDataSummary() { return dataSummary; }
        public void setDataSummary(String dataSummary) { this.dataSummary = dataSummary; }
    }

    private final WellnessSummaryService wellnessSummaryService;

    public WellnessSummaryController(WellnessSummaryService wellnessSummaryService) {
        this.wellnessSummaryService = wellnessSummaryService;
    }

    @PostMapping("/wellness-summary")
    public ResponseEntity<WellnessSummaryEntity> saveSummary(
            @RequestParam String email,
            @RequestBody WellnessSummaryRequest request
    ) {
        WellnessSummaryEntity saved = wellnessSummaryService.saveSummary(
                email,
                request.getProfileSummary(),
                request.getDataSummary()
        );
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/wellness-summary/latest")
    public ResponseEntity<?> getLatestSummary(@RequestParam String email) {
        Optional<WellnessSummaryEntity> summaryOpt =
                wellnessSummaryService.getLatestSummaryByEmail(email);

        if (summaryOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(summaryOpt.get());
    }
}