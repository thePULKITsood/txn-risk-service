package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.history.TransactionHistory;
import com.pulkitsood.risk.model.Decision;
import com.pulkitsood.risk.model.RuleHit;
import com.pulkitsood.risk.model.Transaction;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * Flags cards used too often. A card may make at most {@code maxTransactions} inside
 * {@code window}; any further transaction inside the same window goes to REVIEW.
 */
public class VelocityRule implements Rule {

    private final TransactionHistory history;
    private final int maxTransactions;
    private final Duration window;

    public VelocityRule(TransactionHistory history, int maxTransactions, Duration window) {
        this.history = history;
        this.maxTransactions = maxTransactions;
        this.window = window;
    }

    @Override
    public String id() {
        return "card-velocity";
    }

    @Override
    public Optional<RuleHit> evaluate(Transaction tx, Instant now) {
        int countInWindow = history.countSince(tx.cardId(), now.minus(window));
        if (countInWindow > maxTransactions) {
            return Optional.of(new RuleHit(id(), Decision.REVIEW,
                    "card made " + countInWindow + " transactions in " + window
                            + ", limit is " + maxTransactions));
        }
        return Optional.empty();
    }
}
