package com.aurum.core_banking.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate limiting using Bucket4j.
 *
 * <ul>
 *   <li>Transfer endpoints — 10 requests / minute (stricter; financial operations)</li>
 *   <li>All other API endpoints — 60 requests / minute</li>
 * </ul>
 *
 * SECURITY NOTE: The {@code X-Forwarded-For} header is read for the client IP because
 * the service is expected to run behind a reverse proxy.  In production the proxy must
 * be configured to set/sanitise this header so it cannot be spoofed by clients.
 */
@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private static final int TRANSFER_CAPACITY = 10;
    private static final int DEFAULT_CAPACITY   = 60;

    // Per-IP bucket storage — ConcurrentHashMap handles concurrent requests safely
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        String clientIp = resolveClientIp(request);
        String path     = request.getRequestURI();

        Bucket bucket = buckets.computeIfAbsent(clientIp, k -> buildBucket(path));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            response.addHeader("X-Rate-Limit-Remaining",
                    String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        long retryAfterSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000L;
        response.addHeader("X-Rate-Limit-Retry-After-Seconds",
                String.valueOf(retryAfterSeconds));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.getWriter().write(
                "{\"title\":\"Too Many Requests\"," +
                "\"detail\":\"Rate limit exceeded. Retry after " + retryAfterSeconds + "s\"}");

        log.warn("Rate limit exceeded for IP={} path={}", clientIp, path);
        return false;
    }

    private Bucket buildBucket(String path) {
        int capacity = path.contains("/transfers") ? TRANSFER_CAPACITY : DEFAULT_CAPACITY;
        return Bucket.builder()
                     .addLimit(Bandwidth.classic(
                             capacity, Refill.intervally(capacity, Duration.ofMinutes(1))))
                     .build();
    }

    /**
     * Returns the originating client IP.
     * Trusts {@code X-Forwarded-For} only when present (set by the load balancer / proxy).
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
