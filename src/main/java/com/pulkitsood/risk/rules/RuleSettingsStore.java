package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.config.RiskProperties;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicReference;

/** Holds the active {@link RuleSettings}; swaps are atomic so readers never see a half-applied update. */
@Component
public class RuleSettingsStore {

    private final AtomicReference<RuleSettings> current;

    public RuleSettingsStore(RiskProperties properties) {
        this.current = new AtomicReference<>(RuleSettings.from(properties));
    }

    public RuleSettings current() {
        return current.get();
    }

    public void replace(RuleSettings settings) {
        current.set(settings);
    }
}
