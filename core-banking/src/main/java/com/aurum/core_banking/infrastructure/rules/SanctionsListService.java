package com.aurum.core_banking.infrastructure.rules;

import org.springframework.stereotype.Service;

/**
 * Screens customer names and national IDs against sanctions lists.
 * Returns a non-null match description when a sanctions hit is found, or null when clear.
 */
@Service
public class SanctionsListService {

    public String screen(String fullName, String nationalId) {
        // TODO: integrate with OFAC/UN/EU sanctions list provider
        return null;
    }
}
