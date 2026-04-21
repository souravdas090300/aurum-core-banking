package com.aurum.core_banking.infrastructure.security;

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
 * and maps them to Spring Security {@code ROLE_} prefixed {@link GrantedAuthority}s.
 *
 * <p>Keycloak embeds roles as a nested object: {@code {"realm_access":{"roles":["ROLE_TELLER"]}}}.
 * Spring's built-in {@code JwtGrantedAuthoritiesConverter} only handles flat string-list claims,
 * so this custom converter is required.
 */
public class KeycloakRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    @SuppressWarnings("unchecked")
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        Map<String, Object> realmAccess =
                (Map<String, Object>) jwt.getClaims()
                                         .getOrDefault("realm_access", Collections.emptyMap());

        List<String> roles = (List<String>) realmAccess
                .getOrDefault("roles", Collections.emptyList());

        return roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
    }
}
