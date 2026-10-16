package com.pulkitsood.risk.ratelimit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * Rejects requests over the per-client limit with 429. Clients identify themselves with
 * the {@code X-Client-Id} header; requests without it are rejected with 400.
 */
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    public static final String CLIENT_ID_HEADER = "X-Client-Id";

    private final RateLimiter rateLimiter;
    private final RateLimitProperties properties;

    public RateLimitInterceptor(RateLimiter rateLimiter, RateLimitProperties properties) {
        this.rateLimiter = rateLimiter;
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws IOException {
        if (!properties.enabled()) {
            return true;
        }
        String clientId = request.getHeader(CLIENT_ID_HEADER);
        if (clientId == null || clientId.isBlank()) {
            writeProblem(response, HttpStatus.BAD_REQUEST, "Missing " + CLIENT_ID_HEADER + " header");
            return false;
        }
        RateLimiter.Result result = rateLimiter.tryAcquire(clientId);
        if (!result.allowed()) {
            response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(result.retryAfterMillis()));
            writeProblem(response, HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded");
            return false;
        }
        return true;
    }

    private static void writeProblem(HttpServletResponse response, HttpStatus status, String title)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("{\"status\":" + status.value() + ",\"title\":\"" + title + "\"}");
    }
}
