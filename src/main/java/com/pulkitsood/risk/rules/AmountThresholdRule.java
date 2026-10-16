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
        if (tx.amount().compareTo(blockAmount) > 0) {
            return Optional.of(new RuleHit(id(), Decision.BLOCK,
                    "amount " + tx.amount() + " is above block limit " + blockAmount));
        }
        if (tx.amount().compareTo(reviewAmount) > 0) {
            return Optional.of(new RuleHit(id(), Decision.REVIEW,
                    "amount " + tx.amount() + " is above review limit " + reviewAmount));
        }
        return Optional.empty();
    }
}
