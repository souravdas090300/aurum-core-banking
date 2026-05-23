package com.aurum.corebanking.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Extracts Keycloak realm roles from the {@code realm_access.roles} JWT claim
 * and converts them to Spring Security {@code GrantedAuthority} objects with
 * the {@code ROLE_} prefix.
 *
 * <p>Keycloak JWT structure:
 * <pre>
 * {
 *   "realm_access": {
 *     "roles": ["banking-user", "loan-officer", "compliance-officer"]
 *   }
 * }
 * </pre>
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess =
                (Map<String, Object>) jwt.getClaims().get("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")) {
            return Collections.emptyList();
        }

        List<String> roles = (List<String>) realmAccess.get("roles");
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase().replace('-', '_')))
                .collect(Collectors.toList());
    }
}
