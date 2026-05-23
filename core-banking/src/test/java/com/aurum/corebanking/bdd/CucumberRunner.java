package com.aurum.corebanking.bdd;

import org.junit.platform.suite.api.*;

/**
 * Cucumber JUnit 5 suite runner.
 *
 * <p>Discovers all {@code *.feature} files under {@code classpath:features/} and runs them
 * using step definitions found in the {@code com.aurum.corebanking.bdd} glue package.
 *
 * <p>Reports are generated in:
 * <ul>
 *   <li>HTML: {@code target/cucumber-reports/cucumber.html}</li>
 *   <li>JSON: {@code target/cucumber-reports/cucumber.json} (for CI integration)</li>
 * </ul>
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
    key   = "cucumber.glue",
    value = "com.aurum.corebanking.bdd,com.aurum.corebanking.bdd.steps")
@ConfigurationParameter(
    key   = "cucumber.plugin",
    value = "pretty," +
            "html:target/cucumber-reports/cucumber.html," +
            "json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(
    key   = "cucumber.publish.quiet",
    value = "true")
public class CucumberRunner {
    // Intentionally empty — JUnit Platform Suite runs the suite declaratively
}
