package com.pulkitsood.risk.api;

import com.pulkitsood.risk.engine.RiskEngine;
import com.pulkitsood.risk.model.RiskDecision;
import com.pulkitsood.risk.model.Transaction;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/risk")
public class RiskController {

    private final RiskEngine engine;

    public RiskController(RiskEngine engine) {
        this.engine = engine;
    }

    @PostMapping("/check")
    public RiskDecision check(@Valid @RequestBody Transaction tx) {
        return engine.evaluate(tx);
    }
}
