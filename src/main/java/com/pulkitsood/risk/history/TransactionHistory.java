package com.pulkitsood.risk.history;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * In-memory record of recent transaction times per card, used by velocity rules.
 * Entries older than {@code retention} are pruned on every write.
 *
 * <p>Single-node only: a multi-instance deployment needs a shared store (e.g. Redis sorted sets).
 */
@Component
public class TransactionHistory {

    private static final Duration DEFAULT_RETENTION = Duration.ofHours(1);

    private final ConcurrentMap<String, Deque<Instant>> byCard = new ConcurrentHashMap<>();
    private final Duration retention;

    public TransactionHistory() {
        this(DEFAULT_RETENTION);
    }

    public TransactionHistory(Duration retention) {
        this.retention = retention;
    }

    public void record(String cardId, Instant at) {
        Deque<Instant> times = byCard.computeIfAbsent(cardId, k -> new ArrayDeque<>());
        synchronized (times) {
            times.addLast(at);
            Instant cutoff = at.minus(retention);
            while (!times.isEmpty() && times.peekFirst().isBefore(cutoff)) {
                times.pollFirst();
            }
        }
    }

    /** Number of recorded transactions for the card at or after {@code since}. */
    public int countSince(String cardId, Instant since) {
        Deque<Instant> times = byCard.get(cardId);
        if (times == null) {
            return 0;
        }
        synchronized (times) {
            int count = 0;
            for (var it = times.descendingIterator(); it.hasNext(); ) {
                if (it.next().isBefore(since)) {
                    break;
                }
                count++;
            }
            return count;
        }
    }
}
