package com.pulkitsood.risk.ratelimit;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Per-client rate limit for the risk API.
 *
 * @param enabled         turn limiting off entirely (e.g. for load tests)
 * @param capacity        burst size: max requests a client can make at once
 * @param refillPerSecond sustained rate: tokens added back per second
 */
@ConfigurationProperties(prefix = "rate-limit")
public record RateLimitProperties(boolean enabled, int capacity, double refillPerSecond) {
}
