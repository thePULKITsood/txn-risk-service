package com.pulkitsood.risk.model;

/**
 * Outcome of a risk check, ordered from least to most severe.
 * The engine always returns the most severe decision raised by any rule.
 */
public enum Decision {
    ALLOW,
    REVIEW,
    BLOCK;

    public Decision mostSevere(Decision other) {
        return this.compareTo(other) >= 0 ? this : other;
    }
}
