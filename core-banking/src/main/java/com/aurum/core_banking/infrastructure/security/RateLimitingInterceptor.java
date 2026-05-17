package com.aurum.core_banking.infrastructure.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-IP rate-limiting interceptor for the Transfer API.
 *
 * <p>Uses Bucket4j token-bucket algorithm to allow a configurable number of
 * requests per minute per client IP. Returns {@code 429 Too Many Requests}
 * when the bucket is exhausted.
 */
@Slf4j
@Component
public class RateLimitingInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

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
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
                             @NonNull HttpServletResponse response,
                             @NonNull Object handler) throws Exception {
        String clientIp = resolveClientIp(request);
        Bucket bucket = buckets.computeIfAbsent(clientIp, this::newBucket);

        if (bucket.tryConsume(1)) {
            return true;
        }

        log.warn("Rate limit exceeded for IP={} path={}", clientIp, request.getRequestURI());
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.getWriter().write("{\"error\":\"Rate limit exceeded. Please slow down.\"}");
        return false;
    }

    private Bucket newBucket(String clientIp) {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.greedy(refillTokens, Duration.ofSeconds(refillSeconds)));
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].strip()
                : request.getRemoteAddr();
    }
}
