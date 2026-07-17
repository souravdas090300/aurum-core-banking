package com.aurum.corebanking.infrastructure.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RhpamHealthIndicator implements HealthIndicator {

    private final KieContainer kieContainer;

    @Override
    public Health health() {
        try {
            // Test if KieContainer is working
            var kieBase = kieContainer.getKieBase();
            int ruleCount = kieBase.getKiePackages().stream()
                    .mapToInt(p -> p.getRules().size())
                    .sum();

            return Health.up()
                    .withDetail("rulesLoaded", ruleCount)
                    .withDetail("kieContainer", "active")
                    .withDetail("packages", kieBase.getKiePackages().stream()
                            .map(p -> p.getName())
                            .toList())
                    .build();
                    
        } catch (Exception e) {
            log.error("RHPAM health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("kieContainer", "failed")
                    .build();
        }
    }
}