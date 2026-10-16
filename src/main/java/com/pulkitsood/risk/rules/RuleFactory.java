package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.history.TransactionHistory;
import org.springframework.stereotype.Component;

import java.util.List;

/** Builds the rule set from the given settings. */
@Component
public class RuleFactory {

    private final TransactionHistory history;

    public RuleFactory(TransactionHistory history) {
        this.history = history;
    }

    public List<Rule> build(RuleSettings settings) {
        return List.of(
                new BlocklistRule(settings.blockedMerchants(), settings.blockedCountries()),
                new AmountThresholdRule(settings.reviewAmount(), settings.blockAmount()),
                new VelocityRule(history, settings.velocityMaxTransactions(), settings.velocityWindow()));
    }
}
