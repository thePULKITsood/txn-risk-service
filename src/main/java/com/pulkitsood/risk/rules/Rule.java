package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.model.RuleHit;
import com.pulkitsood.risk.model.Transaction;

import java.time.Instant;
import java.util.Optional;

/** A single risk rule. Returns a hit when the rule fires, empty otherwise. */
public interface Rule {

    String id();

    Optional<RuleHit> evaluate(Transaction tx, Instant now);
}
