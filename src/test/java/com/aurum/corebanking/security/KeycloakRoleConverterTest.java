package com.aurum.corebanking.security;

import com.aurum.corebanking.infrastructure.security.KeycloakRoleConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link KeycloakRoleConverter}.
 */
@DisplayName("KeycloakRoleConverter tests")
class KeycloakRoleConverterTest {

    private final KeycloakRoleConverter converter = new KeycloakRoleConverter();

    @Test
    @DisplayName("Keycloak realm roles are mapped to ROLE_UPPERCASE Spring authorities")
    void realmRoles_mappedToSpringAuthorities() {
        Jwt jwt = buildJwt(Map.of("roles", List.of("CUSTOMER", "MANAGER")));

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_CUSTOMER", "ROLE_MANAGER");
    }

    @Test
    @DisplayName("Missing realm_access claim returns empty authority list")
    void missingRealmAccess_returnsEmptyList() {
        Jwt jwt = buildJwt(Collections.emptyMap());

        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        assertThat(authorities).isEmpty();
    }

    private Jwt buildJwt(Map<String, Object> roles) {
        Map<String, Object> claims = new HashMap<>();
        if (!roles.isEmpty()) {
            claims.put("realm_access", roles);
        }
        claims.put("sub", "user-123");

        return Jwt.withTokenValue("token")
                .header("alg", "RS256")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .claims(c -> c.putAll(claims))
                .build();
    }
}
