package com.pulkitsood.risk.ratelimit;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenBucketTest {

    private static final long SECOND = 1_000_000_000L;

    @Test
    void allowsBurstUpToCapacityThenRejects() {
        TokenBucket bucket = new TokenBucket(3, 1, 0);
        assertThat(bucket.tryConsume(0)).isTrue();
        assertThat(bucket.tryConsume(0)).isTrue();
        assertThat(bucket.tryConsume(0)).isTrue();
        assertThat(bucket.tryConsume(0)).isFalse();
    }

    @Test
    void refillsOverTime() {
        TokenBucket bucket = new TokenBucket(2, 2, 0);
        bucket.tryConsume(0);
        bucket.tryConsume(0);
        assertThat(bucket.tryConsume(0)).isFalse();
        assertThat(bucket.tryConsume(SECOND / 2)).isTrue();
    }

    @Test
    void neverRefillsAboveCapacity() {
        TokenBucket bucket = new TokenBucket(2, 100, 0);
        assertThat(bucket.availableTokens(60 * SECOND)).isEqualTo(2.0);
    }

    @Test
    void reportsWaitUntilNextToken() {
        TokenBucket bucket = new TokenBucket(1, 4, 0);
        bucket.tryConsume(0);
        assertThat(bucket.nanosUntilNextToken(0)).isEqualTo(SECOND / 4);
    }
}
