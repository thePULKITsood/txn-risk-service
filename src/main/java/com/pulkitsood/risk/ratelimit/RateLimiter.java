package com.pulkitsood.risk.ratelimit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.LongSupplier;

/** Keeps one {@link TokenBucket} per API client. */
@Component
public class RateLimiter {

    private final RateLimitProperties properties;
    private final LongSupplier nanoClock;
    private final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    @Autowired
    public RateLimiter(RateLimitProperties properties) {
        this(properties, System::nanoTime);
    }

    RateLimiter(RateLimitProperties properties, LongSupplier nanoClock) {
        this.properties = properties;
        this.nanoClock = nanoClock;
    }

    public Result tryAcquire(String clientId) {
        long now = nanoClock.getAsLong();
        TokenBucket bucket = buckets.computeIfAbsent(clientId,
                id -> new TokenBucket(properties.capacity(), properties.refillPerSecond(), now));
        if (bucket.tryConsume(now)) {
            return Result.ALLOWED;
        }
        return Result.rejected(bucket.nanosUntilNextToken(now) / 1_000_000);
    }

    /** Outcome of an acquire attempt. {@code retryAfterMillis} is 0 when allowed. */
    public record Result(boolean allowed, long retryAfterMillis) {
        static final Result ALLOWED = new Result(true, 0);

        static Result rejected(long retryAfterMillis) {
            return new Result(false, Math.max(1, retryAfterMillis));
        }
    }
}
