package com.aurum.corebanking.infrastructure.config;

import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.Map;

@Configuration
public class ActuatorConfig {

    @Bean
    public InfoContributor bankingInfoContributor() {
        return builder -> builder.withDetail("banking", Map.of(
            "name",        "Aurum Core Banking System",
            "version",     "1.0.0",
            "compliance",  Map.of(
                "gdpr",  "enabled",
                "psd2",  "enabled",
                "aml",   "enabled",
                "fiau",  "Malta FIAU reporting active"
            ),
            "features", Map.of(
                "fraudDetection", "Drools 9 RHPAM",
                "loanWorkflow",   "jBPM BPMN2",
                "authentication", "Keycloak 24 OAuth2/OIDC",
                "rateLimit",      "Bucket4j 8.x"
            ),
            "startedAt", Instant.now().toString()
        ));
    }
}