package com.qavzuro.security;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory, per-IP token-bucket rate limiter for sensitive endpoints.
 * In-memory buckets are fine for a single backend instance; a multi-instance
 * deployment should back this with a shared store (e.g. Redis) instead.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${app.rate-limit.login.capacity:5}")
    private int loginCapacity;
    @Value("${app.rate-limit.login.refill-per-minute:5}")
    private int loginRefill;

    @Value("${app.rate-limit.register.capacity:3}")
    private int registerCapacity;
    @Value("${app.rate-limit.register.refill-per-minute:3}")
    private int registerRefill;

    @Value("${app.rate-limit.default.capacity:60}")
    private int defaultCapacity;
    @Value("${app.rate-limit.default.refill-per-minute:60}")
    private int defaultRefill;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();
        String ip = clientIp(request);

        String bucketKey;
        int capacity;
        int refillPerMinute;

        if (path.equals("/api/auth/login")) {
            bucketKey = "login:" + ip;
            capacity = loginCapacity;
            refillPerMinute = loginRefill;
        } else if (path.equals("/api/auth/register")) {
            bucketKey = "register:" + ip;
            capacity = registerCapacity;
            refillPerMinute = registerRefill;
        } else if (path.startsWith("/api/auth/refresh") || path.startsWith("/api/payments") || path.startsWith("/api/products/search")) {
            bucketKey = "sensitive:" + ip + ":" + path;
            capacity = defaultCapacity;
            refillPerMinute = defaultRefill;
        } else {
            chain.doFilter(request, response);
            return;
        }

        Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> Bucket.builder()
                .addLimit(Bandwidth.classic(capacity, Refill.greedy(refillPerMinute, Duration.ofMinutes(1))))
                .build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"Rate limit exceeded, please try again later.\"}");
        }
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
