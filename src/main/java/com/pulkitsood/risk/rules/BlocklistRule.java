package com.pulkitsood.risk.rules;

import com.pulkitsood.risk.model.Decision;
import com.pulkitsood.risk.model.RuleHit;
import com.pulkitsood.risk.model.Transaction;

import java.time.Instant;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/** Blocks transactions to blocklisted merchants or merchants in blocklisted countries. */
public class BlocklistRule implements Rule {

    private final Set<String> blockedMerchants;
    private final Set<String> blockedCountries;

    public BlocklistRule(Collection<String> blockedMerchants, Collection<String> blockedCountries) {
        this.blockedMerchants = Set.copyOf(blockedMerchants);
        this.blockedCountries = blockedCountries.stream()
                .map(c -> c.toUpperCase(Locale.ROOT))
                .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String id() {
        return "blocklist";
    }

    @Override
    public Optional<RuleHit> evaluate(Transaction tx, Instant now) {
        if (blockedMerchants.contains(tx.merchantId())) {
            return Optional.of(new RuleHit(id(), Decision.BLOCK, "merchant " + tx.merchantId() + " is blocklisted"));
        }
        String country = tx.country().toUpperCase(Locale.ROOT);
        if (blockedCountries.contains(country)) {
            return Optional.of(new RuleHit(id(), Decision.BLOCK, "country " + country + " is blocklisted"));
        }
        return Optional.empty();
    }
}
