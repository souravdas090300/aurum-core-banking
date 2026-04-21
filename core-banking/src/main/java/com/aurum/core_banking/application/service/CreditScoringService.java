package com.aurum.core_banking.application.service;

import com.aurum.core_banking.domain.rules.CustomerFact;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoringService {

    private final KieContainer kieContainer;

    public String evaluate(CustomerFact customerFact) {
        KieSession session = kieContainer.newKieSession();
        try {
            session.insert(customerFact);
            int fired = session.fireAllRules();
            log.debug("Credit scoring: {} rules fired for customerId={}", fired, customerFact.getCustomerId());
        } finally {
            session.dispose();
        }
        return customerFact.getLoanDecision();
    }
}
