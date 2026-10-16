package com.pulkitsood.risk.ratelimit;

/**
 * Classic token bucket. Starts full, refills continuously at {@code refillPerSecond},
 * never holds more than {@code capacity} tokens. Each request costs one token.
 */
public class TokenBucket {

    private static final double NANOS_PER_SECOND = 1_000_000_000d;

    private final int capacity;
    private final double refillPerNano;
    private double tokens;
    private long lastRefillNanos;

    public TokenBucket(int capacity, double refillPerSecond, long nowNanos) {
        this.capacity = capacity;
        this.refillPerNano = refillPerSecond / NANOS_PER_SECOND;
        this.tokens = capacity;
        this.lastRefillNanos = nowNanos;
    }

    /** Takes one token if available. */
    public boolean tryConsume(long nowNanos) {
        refill(nowNanos);
        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    /** How long until the next token is available, in nanoseconds; 0 if one is available now. */
    public long nanosUntilNextToken(long nowNanos) {
        refill(nowNanos);
        if (tokens >= 1) {
            return 0;
        }
        return (long) Math.ceil((1 - tokens) / refillPerNano);
    }

    double availableTokens(long nowNanos) {
        refill(nowNanos);
        return tokens;
    }

    private void refill(long nowNanos) {
        long elapsed = nowNanos - lastRefillNanos;
        if (elapsed > 0) {
            tokens = Math.min(capacity, tokens + elapsed * refillPerNano);
            lastRefillNanos = nowNanos;
        }
    }
}
