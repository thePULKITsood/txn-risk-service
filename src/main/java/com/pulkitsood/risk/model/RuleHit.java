package com.pulkitsood.risk.model;

/** A rule that fired for a transaction, with the decision it asks for and why. */
public record RuleHit(String ruleId, Decision decision, String reason) {
}
