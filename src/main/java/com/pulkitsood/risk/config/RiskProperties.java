package com.pulkitsood.risk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/** Startup rule settings, bound from the {@code risk.*} properties. */
@ConfigurationProperties(prefix = "risk")
public record RiskProperties(
        BigDecimal reviewAmount,
        BigDecimal blockAmount,
        int velocityMaxTransactions,
        Duration velocityWindow,
        List<String> blockedMerchants,
        List<String> blockedCountries) {
}
