package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.config.RiskProperties;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * The rule settings currently in force. Can be replaced at runtime through the admin API.
 */
public record RuleSettings(
        @NotNull @Positive BigDecimal reviewAmount,
        @NotNull @Positive BigDecimal blockAmount,
        @Positive int velocityMaxTransactions,
        @NotNull Duration velocityWindow,
        @NotNull List<String> blockedMerchants,
        @NotNull List<String> blockedCountries) {

    public RuleSettings {
        blockedMerchants = blockedMerchants == null ? List.of() : List.copyOf(blockedMerchants);
        blockedCountries = blockedCountries == null ? List.of() : List.copyOf(blockedCountries);
    }

    @AssertTrue(message = "reviewAmount must be lower than blockAmount")
    public boolean isReviewBelowBlock() {
        return reviewAmount == null || blockAmount == null || reviewAmount.compareTo(blockAmount) < 0;
    }

    public static RuleSettings from(RiskProperties p) {
        return new RuleSettings(p.reviewAmount(), p.blockAmount(), p.velocityMaxTransactions(),
                p.velocityWindow(), p.blockedMerchants(), p.blockedCountries());
    }
}
