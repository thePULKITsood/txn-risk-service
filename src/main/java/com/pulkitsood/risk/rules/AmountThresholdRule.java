package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.model.Decision;
import com.pulkitsood.risk.model.RuleHit;
import com.pulkitsood.risk.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Flags large transactions. Amounts at or above {@code reviewAmount} go to REVIEW,
 * amounts at or above {@code blockAmount} are blocked.
 */
public class AmountThresholdRule implements Rule {

    private final BigDecimal reviewAmount;
    private final BigDecimal blockAmount;

    public AmountThresholdRule(BigDecimal reviewAmount, BigDecimal blockAmount) {
        this.reviewAmount = reviewAmount;
        this.blockAmount = blockAmount;
    }

    @Override
    public String id() {
        return "amount-threshold";
    }

    @Override
    public Optional<RuleHit> evaluate(Transaction tx, Instant now) {
        BigDecimal amount = tx.amount();
        if (atOrAbove(amount, blockAmount)) {
            return Optional.of(new RuleHit(id(), Decision.BLOCK,
                    "amount " + amount + " is at or above block limit " + blockAmount));
        }
        if (atOrAbove(amount, reviewAmount)) {
            return Optional.of(new RuleHit(id(), Decision.REVIEW,
                    "amount " + amount + " is at or above review limit " + reviewAmount));
        }
        return Optional.empty();
    }

    /** Thresholds are inclusive: an amount exactly on the limit counts as reaching it. */
    private static boolean atOrAbove(BigDecimal amount, BigDecimal limit) {
        return amount.compareTo(limit) > 0 || amount.equals(limit);
    }
}
