package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.model.Decision;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static com.pulkitsood.risk.TestTransactions.tx;
import static org.assertj.core.api.Assertions.assertThat;

class AmountThresholdRuleTest {

    private final AmountThresholdRule rule =
            new AmountThresholdRule(new BigDecimal("50000"), new BigDecimal("200000"));

    @Test
    void smallAmountPasses() {
        assertThat(rule.evaluate(tx("card-a", "1200.50"), Instant.now())).isEmpty();
    }

    @Test
    void largeAmountGoesToReview() {
        var hit = rule.evaluate(tx("card-a", "75000"), Instant.now());
        assertThat(hit).hasValueSatisfying(h -> assertThat(h.decision()).isEqualTo(Decision.REVIEW));
    }

    @Test
    void hugeAmountIsBlocked() {
        var hit = rule.evaluate(tx("card-a", "250000"), Instant.now());
        assertThat(hit).hasValueSatisfying(h -> assertThat(h.decision()).isEqualTo(Decision.BLOCK));
    }
}
