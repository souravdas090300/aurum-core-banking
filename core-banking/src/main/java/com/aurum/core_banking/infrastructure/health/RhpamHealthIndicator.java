package com.aurum.core_banking.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for the Drools/jBPM (RHPAM) rule engine.
 * Visible at /actuator/health under the key "rhpam".
 *
 * Example output:
 * {
 *   "status": "UP",
 *   "components": {
 *     "rhpam": {
 *       "status": "UP",
 *       "details": {
 *         "rulesLoaded": 12,
 *         "kieContainer": "active",
 *         "packages": ["com.aurum.rules"]
 *       }
 *     }
 *   }
 * }
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RhpamHealthIndicator extends AbstractHealthIndicator {

    private final KieContainer kieContainer;

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        try {
            // Create a temporary session to verify the rule engine is operational
            var session = kieContainer.newKieSession();
            int ruleCount = session.getKieBase()
                    .getKiePackages().stream()
                    .mapToInt(p -> p.getRules().size())
                    .sum();
            var packageNames = session.getKieBase()
                    .getKiePackages().stream()
                    .map(p -> p.getName())
                    .toList();
            session.dispose();

            builder.up()
                    .withDetail("rulesLoaded",  ruleCount)
                    .withDetail("kieContainer", "active")
                    .withDetail("packages",     packageNames);

        } catch (Exception e) {
            log.warn("RHPAM health check failed: {}", e.getMessage());
            builder.down()
                    .withDetail("error", e.getMessage());
        }
    }
}
