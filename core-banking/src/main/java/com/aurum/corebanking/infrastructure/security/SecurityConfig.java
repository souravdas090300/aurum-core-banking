package com.aurum.corebanking.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 6 configuration — stateless JWT resource server.
 *
 * <p>Role mappings:
 * <ul>
 *   <li>BANKING_USER — read own accounts, initiate transfers</li>
 *   <li>LOAN_OFFICER — review and approve/decline loan applications</li>
 *   <li>COMPLIANCE_OFFICER — view audit trail, FIAU reports</li>
 * </ul>
 *
 * <p>Roles are extracted from Keycloak's {@code realm_access.roles} claim
 * via {@link KeycloakRoleConverter}.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@org.springframework.context.annotation.Profile("!dev")
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public health/info endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Account endpoints — banking users
                .requestMatchers(HttpMethod.GET,  "/api/v1/accounts/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER", "COMPLIANCE_OFFICER")
                // Transfer initiation
                .requestMatchers(HttpMethod.POST, "/api/v1/transfers")
                    .hasRole("BANKING_USER")
                // Loan officer actions
                .requestMatchers("/api/v1/loans/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER")
                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
