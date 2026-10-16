package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.model.Decision;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

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

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
            "49999,  NONE",
            "49999.99, NONE",
            "50000,  REVIEW",
            "50000.0, REVIEW",
            "50000.00, REVIEW",
            "50001,  REVIEW",
            "75000,  REVIEW",
            "199999, REVIEW",
            "200000, BLOCK",
            "200000.00, BLOCK",
            "250000, BLOCK"
    })
    void thresholdsAreInclusive(String amount, String expected) {
        var hit = rule.evaluate(tx("card-a", amount), Instant.now());
        if (expected.equals("NONE")) {
            assertThat(hit).isEmpty();
        } else {
            assertThat(hit).hasValueSatisfying(h -> assertThat(h.decision()).isEqualTo(Decision.valueOf(expected)));
        }
    }

    @Test
    void reasonMentionsTheLimit() {
        var hit = rule.evaluate(tx("card-a", "50000"), Instant.now());
        assertThat(hit).hasValueSatisfying(h -> assertThat(h.reason()).contains("at or above review limit 50000"));
    }
}
