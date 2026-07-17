package com.aurum.corebanking.interfaces.rest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.time.Instant;

@RestController
public class PublicController {
    
    @GetMapping("/api/public/status")
    public Map<String, Object> status() {
        return Map.of(
            "status", "RUNNING",
            "timestamp", Instant.now().toString(),
            "service", "Aurum Core Banking",
            "version", "1.0.0"
        );
    }
    
    @GetMapping("/api/public/health")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}