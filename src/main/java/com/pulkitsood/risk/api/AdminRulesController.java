package com.pulkitsood.risk.api;

import com.pulkitsood.risk.engine.RiskEngine;
import com.pulkitsood.risk.rules.RuleSettings;
import com.pulkitsood.risk.rules.RuleSettingsStore;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** Read and replace the active rule settings at runtime. Intended for internal/ops use only. */
@RestController
@RequestMapping("/v1/admin/rules")
public class AdminRulesController {

    private final RuleSettingsStore store;
    private final RiskEngine engine;

    public AdminRulesController(RuleSettingsStore store, RiskEngine engine) {
        this.store = store;
        this.engine = engine;
    }

    @GetMapping
    public Map<String, Object> get() {
        List<String> ruleIds = engine.activeRuleIds();
        return Map.of("settings", store.current(), "rules", ruleIds);
    }

    @PutMapping
    public RuleSettings replace(@Valid @RequestBody RuleSettings settings) {
        store.replace(settings);
        return store.current();
    }
}
