# txn-risk-service

A small real-time **transaction risk check API**. A payment client posts a card transaction and gets back
`ALLOW`, `REVIEW` or `BLOCK`, plus every rule that fired and why.

Built with Java 21 and Spring Boot 3.

## Endpoints

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/v1/risk/check` | Evaluate a transaction |
| `GET` | `/v1/admin/rules` | Show active rule settings and rule ids |
| `PUT` | `/v1/admin/rules` | Replace rule settings at runtime |

```bash
curl -s localhost:8080/v1/risk/check -H 'Content-Type: application/json' -H "X-Client-Id: acme-pay" -d '{
  "transactionId": "t-1001", "clientId": "acme-pay", "cardId": "tok_4f9a",
  "merchantId": "m-grocer-17", "amount": 64000, "country": "IN"
}'
```

```json
{
  "transactionId": "t-1001",
  "decision": "REVIEW",
  "hits": [{"ruleId": "amount-threshold", "decision": "REVIEW", "reason": "amount 64000 is above review limit 50000"}],
  "evaluatedAt": "2026-10-17T10:00:00Z"
}
```

Invalid input returns `400` with an RFC 7807 problem body listing each field error.

### Rate limiting

Calls to `/v1/risk/**` must send an `X-Client-Id` header (missing → `400`). Each client gets a token bucket
(`rate-limit.capacity` burst, `rate-limit.refill-per-second` sustained). Over the limit the API returns `429` with a
`Retry-After` header.

## Rules

| Rule | Fires when | Decision |
|---|---|---|
| `blocklist` | merchant or merchant country is blocklisted | `BLOCK` |
| `amount-threshold` | amount is at or above `review-amount` / `block-amount` | `REVIEW` / `BLOCK` |
| `card-velocity` | the card exceeds `velocity-max-transactions` inside `velocity-window` | `REVIEW` |

All rules run on every request; the most severe decision wins and all hits are returned so an analyst sees
every reason. Settings live in `application.yml` under `risk.*` and can be swapped at runtime via the admin API.

## Design notes

- `RiskEngine` is the only orchestration point; rules are small, stateless classes behind the `Rule` interface.
- Card velocity uses an in-memory sliding window (`TransactionHistory`). This is single-node only; a multi-instance
  deployment would move it to a shared store such as Redis sorted sets.
- Rule settings are held in an `AtomicReference`, so an admin update is applied atomically.
- Amounts are `BigDecimal` and single-currency (INR) for now.

## Run

```bash
mvn spring-boot:run      # starts on :8080
mvn verify               # unit + API tests
```
