package com.aurum.core_banking.infrastructure.rules;

import lombok.extern.slf4j.Slf4j;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class DroolsConfig {

    @Bean
    public KieContainer kieContainer() {
        KieContainer container = KieServices.Factory.get().getKieClasspathContainer();
        log.info("Drools KieContainer initialized — KieBases: {}", container.getKieBaseNames());
        return container;
    }
}
