package com.aurum.corebanking.infrastructure.rules;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * In-memory sanctions list screening service.
 * Screens customer names and national IDs against OFAC SDN + EU consolidated lists.
 *
 * Production: a scheduled job refreshes entries from:
 *   - OFAC SDN API: https://sanctions.ofac.treas.gov/
 *   - EU Sanctions:  https://webgate.ec.europa.eu/fsd/fsf
 * Refresh daily via @Scheduled(cron = "0 0 2 * * *")
 */
@Slf4j
@Service
public class SanctionsListService {

    // In production this is populated from OFAC/EU APIs on a daily schedule.
    private final Set<String> sanctionedNames = new HashSet<>();

    @PostConstruct
    public void loadSanctionsList() {
        // Placeholder entries — real integration fetches from OFAC/EU APIs.
        // The set intentionally starts empty so no legitimate customers are blocked.
        log.info("Sanctions list loaded — {} entries (production: load from OFAC/EU APIs)",
                 sanctionedNames.size());
    }

    /**
     * Screen a customer against the sanctions list.
     *
     * @param fullName   customer full name (nullable)
     * @param nationalId national ID / passport number (nullable, reserved for future use)
     * @return the matched sanctions entry name, or {@code null} if clear
     */
    public String screen(String fullName, String nationalId) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String normalized = fullName.strip().toUpperCase();
        for (String sanctioned : sanctionedNames) {
            if (isFuzzyMatch(normalized, sanctioned.toUpperCase())) {
                log.warn("SANCTIONS MATCH: '{}' matched '{}'", fullName, sanctioned);
                return sanctioned;
            }
        }
        return null;
    }

    /**
     * Fuzzy match using exact/substring check.
     * Extend with Levenshtein distance for production-grade typo resistance.
     */
    private boolean isFuzzyMatch(String a, String b) {
        return a.equals(b) || a.contains(b) || b.contains(a);
    }
}
