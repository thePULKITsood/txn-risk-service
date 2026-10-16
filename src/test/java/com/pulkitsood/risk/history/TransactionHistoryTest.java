package com.pulkitsood.risk.history;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionHistoryTest {

    private final Instant t0 = Instant.parse("2026-10-17T10:00:00Z");

    @Test
    void countsOnlyEntriesSinceTheCutoff() {
        TransactionHistory history = new TransactionHistory();
        history.record("c1", t0);
        history.record("c1", t0.plusSeconds(60));
        history.record("c1", t0.plusSeconds(120));

        assertThat(history.countSince("c1", t0.plusSeconds(60))).isEqualTo(2);
        assertThat(history.countSince("c1", t0)).isEqualTo(3);
        assertThat(history.countSince("unknown", t0)).isZero();
    }

    @Test
    void prunesEntriesOlderThanRetention() {
        TransactionHistory history = new TransactionHistory(Duration.ofMinutes(5));
        history.record("c1", t0);
        history.record("c1", t0.plus(Duration.ofMinutes(10)));

        assertThat(history.countSince("c1", Instant.EPOCH)).isEqualTo(1);
    }
}
