package com.pulkitsood.risk.engine;

import com.pulkitsood.risk.history.TransactionHistory;
import com.pulkitsood.risk.model.Decision;
import com.pulkitsood.risk.model.RiskDecision;
import com.pulkitsood.risk.model.RuleHit;
import com.pulkitsood.risk.model.Transaction;
import com.pulkitsood.risk.rules.Rule;
import com.pulkitsood.risk.rules.RuleFactory;
import com.pulkitsood.risk.rules.RuleSettingsStore;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs every active rule against a transaction and returns the most severe decision.
 * All rules are evaluated (no short-circuit) so the response explains every reason.
 *
 * <p>The rule set is compiled once and reused: building it copies and normalises the
 * blocklists, which is wasted work on the hot path.
 */
@Service
public class RiskEngine {

    private final TransactionHistory history;
    private final Clock clock;
    private final List<Rule> rules;

    public RiskEngine(RuleSettingsStore settingsStore, RuleFactory ruleFactory,
                      TransactionHistory history, Clock clock) {
        this.history = history;
        this.clock = clock;
        this.rules = ruleFactory.build(settingsStore.current());
    }

    public RiskDecision evaluate(Transaction tx) {
        Instant now = clock.instant();
        history.record(tx.cardId(), now);

        List<RuleHit> hits = new ArrayList<>();
        Decision decision = Decision.ALLOW;
        for (Rule rule : rules) {
            var hit = rule.evaluate(tx, now);
            if (hit.isPresent()) {
                hits.add(hit.get());
                decision = decision.mostSevere(hit.get().decision());
            }
        }
        return new RiskDecision(tx.transactionId(), decision, List.copyOf(hits), now);
    }

    public List<String> activeRuleIds() {
        return rules.stream().map(Rule::id).toList();
    }
}
