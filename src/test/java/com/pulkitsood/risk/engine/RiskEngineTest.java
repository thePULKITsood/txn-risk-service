package com.pulkitsood.risk.engine;

import com.pulkitsood.risk.config.RiskProperties;
import com.pulkitsood.risk.history.TransactionHistory;
import com.pulkitsood.risk.model.Decision;
import com.pulkitsood.risk.rules.RuleFactory;
import com.pulkitsood.risk.rules.RuleSettingsStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static com.pulkitsood.risk.TestTransactions.tx;
import static org.assertj.core.api.Assertions.assertThat;

class RiskEngineTest {

    private final RiskProperties props = new RiskProperties(new BigDecimal("50000"), new BigDecimal("200000"),
            2, Duration.ofMinutes(10), List.of("m-bad"), List.of("KP"));
    private final TransactionHistory history = new TransactionHistory();
    private final RiskEngine engine = new RiskEngine(new RuleSettingsStore(props), new RuleFactory(history),
            history, Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneOffset.UTC));

    @Test
    void cleanTransactionIsAllowed() {
        var result = engine.evaluate(tx("card-1", "999"));
        assertThat(result.decision()).isEqualTo(Decision.ALLOW);
        assertThat(result.hits()).isEmpty();
    }

    @Test
    void mostSevereDecisionWinsAndAllHitsAreReported() {
        var result = engine.evaluate(tx("card-2", "75000", "m-bad", "IN"));
        assertThat(result.decision()).isEqualTo(Decision.BLOCK);
        assertThat(result.hits()).extracting("ruleId").containsExactlyInAnyOrder("blocklist", "amount-threshold");
    }

    @Test
    void velocityTripsOnTheThirdTransactionWhenLimitIsTwo() {
        assertThat(engine.evaluate(tx("card-3", "10")).decision()).isEqualTo(Decision.ALLOW);
        assertThat(engine.evaluate(tx("card-3", "10")).decision()).isEqualTo(Decision.ALLOW);
        assertThat(engine.evaluate(tx("card-3", "10")).decision()).isEqualTo(Decision.REVIEW);
    }
}
