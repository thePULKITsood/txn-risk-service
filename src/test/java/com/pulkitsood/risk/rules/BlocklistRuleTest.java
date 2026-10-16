package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.model.Decision;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static com.pulkitsood.risk.TestTransactions.tx;
import static org.assertj.core.api.Assertions.assertThat;

class BlocklistRuleTest {

    private final BlocklistRule rule = new BlocklistRule(List.of("m-bad"), List.of("kp"));

    @Test
    void blocksListedMerchant() {
        var hit = rule.evaluate(tx("card-a", "100", "m-bad", "IN"), Instant.now());
        assertThat(hit).hasValueSatisfying(h -> assertThat(h.decision()).isEqualTo(Decision.BLOCK));
    }

    @Test
    void blocksListedCountryCaseInsensitively() {
        assertThat(rule.evaluate(tx("card-a", "100", "m-ok", "KP"), Instant.now())).isPresent();
        assertThat(rule.evaluate(tx("card-a", "100", "m-ok", "kp"), Instant.now())).isPresent();
    }

    @Test
    void allowsEverythingElse() {
        assertThat(rule.evaluate(tx("card-a", "100", "m-ok", "IN"), Instant.now())).isEmpty();
    }
}
