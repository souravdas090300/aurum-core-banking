package com.aurum.corebanking.bdd;

import com.aurum.corebanking.BaseIntegrationTest;
import io.cucumber.spring.CucumberContextConfiguration;

/**
 * Binds the Cucumber Spring context to our shared {@link BaseIntegrationTest} base class.
 *
 * <p>This class must be on the Cucumber glue path (same package as {@code CucumberRunner}).
 * It tells Cucumber to use the Spring application context started by {@link BaseIntegrationTest}.
 */
@CucumberContextConfiguration
public class CucumberSpringConfig extends BaseIntegrationTest {
    // No additional code needed — the annotation is sufficient.
}
