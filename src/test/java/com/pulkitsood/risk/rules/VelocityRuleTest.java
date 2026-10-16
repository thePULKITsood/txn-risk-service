package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.history.TransactionHistory;
import com.pulkitsood.risk.model.Decision;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static com.pulkitsood.risk.TestTransactions.tx;
import static org.assertj.core.api.Assertions.assertThat;

class VelocityRuleTest {

    private final TransactionHistory history = new TransactionHistory();
    private final VelocityRule rule = new VelocityRule(history, 3, Duration.ofMinutes(10));
    private final Instant t0 = Instant.parse("2026-10-17T10:00:00Z");

    @Test
    void allowsUpToTheLimit() {
        for (int i = 0; i < 3; i++) {
            history.record("card-v", t0.plusSeconds(i));
        }
        assertThat(rule.evaluate(tx("card-v", "10"), t0.plusSeconds(3))).isEmpty();
    }

    @Test
    void flagsTheTransactionAfterTheLimit() {
        for (int i = 0; i < 4; i++) {
            history.record("card-v", t0.plusSeconds(i));
        }
        var hit = rule.evaluate(tx("card-v", "10"), t0.plusSeconds(4));
        assertThat(hit).hasValueSatisfying(h -> assertThat(h.decision()).isEqualTo(Decision.REVIEW));
    }

    @Test
    void ignoresTransactionsOutsideTheWindow() {
        for (int i = 0; i < 4; i++) {
            history.record("card-v", t0.plusSeconds(i));
        }
        assertThat(rule.evaluate(tx("card-v", "10"), t0.plus(Duration.ofMinutes(11)))).isEmpty();
    }
}
