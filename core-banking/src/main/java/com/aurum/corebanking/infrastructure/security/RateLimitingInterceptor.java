package com.aurum.corebanking.infrastructure.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Per-IP rate-limiting interceptor for the Transfer API.
 *
 * <p>Uses Bucket4j token-bucket algorithm to allow a configurable number of
 * requests per minute per client IP. Returns {@code 429 Too Many Requests}
 * when the bucket is exhausted.
 *
 * <p>Buckets are stored in a Caffeine cache with automatic eviction after 1 hour
 * of inactivity to prevent unbounded memory growth.
 */
@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Cache<String, Bucket> buckets;

    private final int capacity;
    private final int refillTokens;
    private final int refillSeconds;

    public RateLimitingInterceptor(
            @Value("${rate-limit.transfer.capacity:10}")        int capacity,
            @Value("${rate-limit.transfer.refill-tokens:10}")   int refillTokens,
            @Value("${rate-limit.transfer.refill-seconds:60}")  int refillSeconds) {
        this.capacity      = capacity;
        this.refillTokens  = refillTokens;
        this.refillSeconds = refillSeconds;
        
        // Create bounded cache with automatic eviction after 1 hour of inactivity
        // Maximum 10,000 entries to prevent memory exhaustion
        this.buckets = Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .build();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.get(clientIp, this::newBucket);

        if (bucket.tryConsume(1)) {
            return true;
        }

        log.warn("Rate limit exceeded for IP={} path={}", clientIp, request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("{\"error\":\"Rate limit exceeded. Please slow down.\"}");
        return false;
    }

    private Bucket newBucket(String clientIp) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, Duration.ofSeconds(refillSeconds))
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].strip()
                : request.getRemoteAddr();
    }
}
