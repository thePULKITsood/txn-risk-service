package com.pulkitsood.risk;

import com.pulkitsood.risk.model.Transaction;

import java.math.BigDecimal;
import java.util.UUID;

public final class TestTransactions {

    private TestTransactions() {
    }

    public static Transaction tx(String cardId, String amount) {
        return new Transaction(UUID.randomUUID().toString(), "client-1", cardId, "m-good-001",
                new BigDecimal(amount), "IN");
    }

    public static Transaction tx(String cardId, String amount, String merchantId, String country) {
        return new Transaction(UUID.randomUUID().toString(), "client-1", cardId, merchantId,
                new BigDecimal(amount), country);
    }
}
