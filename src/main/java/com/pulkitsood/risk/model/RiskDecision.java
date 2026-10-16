package com.pulkitsood.risk.model;

import java.time.Instant;
import java.util.List;

/** Response of a risk check. */
public record RiskDecision(
        String transactionId,
        Decision decision,
        List<RuleHit> hits,
        Instant evaluatedAt) {
}
