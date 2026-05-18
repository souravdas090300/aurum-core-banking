package com.aurum.core_banking.infrastructure.config;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;

/**
 * Custom /actuator/info endpoint showing system capabilities and compliance status.
 * Visible to monitoring tools, service registries, and health dashboards.
 */
@Configuration
public class ActuatorConfig {

    @Bean
    public InfoContributor bankingInfoContributor() {
        return builder -> builder.withDetail("banking", Map.of(
            "name",        "Aurum Core Banking System",
            "version",     "1.0.0",
            "environment", "production",
            "compliance",  Map.of(
                "gdpr",    "enabled — Art.17 erasure, Art.15 access, 7yr retention",
                "psd2",    "enabled — SCA (acr=gold) for transfers >€30",
                "aml",     "enabled — Drools 9 velocity + sanctions screening",
                "fiau",    "Malta FIAU PMLA reporting active (≥€10,000)"
            ),
            "features", Map.of(
                "fraudDetection",  "Drools 9 RHPAM rule engine",
                "loanWorkflow",    "jBPM BPMN2 loan approval process",
                "authentication",  "Keycloak 24 OAuth2/OIDC + TOTP MFA",
                "rateLimit",       "Bucket4j 8.x token-bucket per user",
                "encryption",      "AES-256-GCM column-level encryption"
            ),
            "startedAt",  Instant.now().toString()
        ));
    }
}
