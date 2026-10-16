package com.pulkitsood.risk.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * A card transaction submitted for a risk check.
 *
 * @param transactionId caller-supplied id, echoed back in the response
 * @param clientId      the API client (merchant aggregator) making the call
 * @param cardId        tokenised card reference, never a raw PAN
 * @param merchantId    merchant the card is paying
 * @param amount        transaction amount in INR
 * @param country       ISO-3166 alpha-2 country of the merchant
 */
public record Transaction(
        @NotBlank String transactionId,
        @NotBlank String clientId,
        @NotBlank String cardId,
        @NotBlank String merchantId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Pattern(regexp = "[A-Za-z]{2}") String country) {
}
