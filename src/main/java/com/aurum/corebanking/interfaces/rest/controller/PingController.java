package com.aurum.corebanking.interfaces.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/ping")
public class PingController {
    
    @GetMapping
    public Map<String, String> ping() {
        return Map.of("status", "OK", "message", "API is working!");
    }
    
    @GetMapping("/auth")
    public Map<String, Object> authCheck(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "authenticated", true,
            "userId", jwt.getSubject(),
            "username", jwt.getClaimAsString("preferred_username"),
            "roles", jwt.getClaimAsMap("realm_access").get("roles")
        );
    }
}