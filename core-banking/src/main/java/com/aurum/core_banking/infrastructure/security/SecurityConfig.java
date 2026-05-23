package com.aurum.core_banking.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

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
 *
 * <p>Only active in production profile. Dev/test profiles use TestSecurityConfig instead.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("prod")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public health/info endpoints
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Account endpoints — banking users
                .requestMatchers(HttpMethod.GET,  "/api/accounts/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER", "COMPLIANCE_OFFICER")
                .requestMatchers(HttpMethod.POST, "/api/accounts/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER")
                .requestMatchers(HttpMethod.PUT,  "/api/accounts/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER")
                // Transaction endpoints
                .requestMatchers("/api/transactions/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER", "COMPLIANCE_OFFICER")
                // Customer endpoints
                .requestMatchers("/api/customers/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER", "COMPLIANCE_OFFICER")
                // Loan officer actions
                .requestMatchers("/api/loans/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER")
                // Dashboard endpoints
                .requestMatchers("/api/dashboard/**")
                    .hasAnyRole("BANKING_USER", "LOAN_OFFICER", "COMPLIANCE_OFFICER")
                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
        return converter;
    }
}
