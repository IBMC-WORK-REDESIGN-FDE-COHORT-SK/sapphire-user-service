package com.sapphire.userservice.service;

import com.sapphire.userservice.model.UserEntity;
import com.sapphire.userservice.model.WellnessSummaryEntity;
import com.sapphire.userservice.repository.UserRepository;
import com.sapphire.userservice.repository.WellnessSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class WellnessSummaryService {

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WellnessSummaryService.class);

    private final WellnessSummaryRepository wellnessSummaryRepository;
    private final UserRepository userRepository;

    public WellnessSummaryService(
            WellnessSummaryRepository wellnessSummaryRepository,
            UserRepository userRepository
    ) {
        this.wellnessSummaryRepository = wellnessSummaryRepository;
        this.userRepository = userRepository;
    }

    public WellnessSummaryEntity saveSummary(String email, String profileSummary, String dataSummary) {
        log.info("Saving wellness summary for email={}", email);
        log.info("Profile summary length={}, Data summary length={}",
                profileSummary != null ? profileSummary.length() : 0,
                dataSummary != null ? dataSummary.length() : 0);

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found for email: " + email);
        }

        UUID userId = userOpt.get().getId();

        WellnessSummaryEntity entity = new WellnessSummaryEntity();
        entity.setUserId(userId);
        entity.setProfileSummary(profileSummary);
        entity.setDataSummary(dataSummary);

        return wellnessSummaryRepository.save(entity);
    }

    public Optional<WellnessSummaryEntity> getLatestSummaryByEmail(String email) {
        log.info("Fetching latest wellness summary for email={}", email);

        Optional<UserEntity> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }

        UUID userId = userOpt.get().getId();

        return wellnessSummaryRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .findFirst();
    }
}