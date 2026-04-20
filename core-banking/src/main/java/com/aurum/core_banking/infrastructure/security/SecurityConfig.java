package com.aurum.core_banking.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)  // enables @PreAuthorize on methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Session: STATELESS — JWT carries all state ────────────────────
            .sessionManagement(s ->
                s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── CSRF: disabled for stateless REST API ─────────────────────────
            .csrf(csrf -> csrf.disable())

            // ── Endpoint authorisation ────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                // All remaining actuator endpoints — ADMIN only
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                // Everything else requires a valid JWT at minimum;
                // fine-grained RBAC is enforced via @PreAuthorize per method
                .anyRequest().authenticated()
            )

            // ── OAuth2 Resource Server — validate Keycloak JWTs ──────────────
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter()))
            )

            // ── OWASP Security Headers ────────────────────────────────────────
            .headers(headers -> headers
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31_536_000))
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(ct -> {})
                .referrerPolicy(ref ->
                    ref.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                .addHeaderWriter(new StaticHeadersWriter(
                    "Permissions-Policy", "camera=(), microphone=(), geolocation=()"))
            );

        return http.build();
    }

    /**
     * Wires our {@link KeycloakRoleConverter} so that JWT authentication tokens
     * carry proper {@code ROLE_} authorities drawn from {@code realm_access.roles}.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
