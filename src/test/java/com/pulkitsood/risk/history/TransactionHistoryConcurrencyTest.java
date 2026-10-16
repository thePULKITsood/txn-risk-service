package com.pulkitsood.risk.history;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link TransactionHistory} is shared by every request thread, so concurrent writes to the
 * same card must never be lost and reads must never throw while writes are in flight.
 */
class TransactionHistoryConcurrencyTest {

    private static final int THREADS = 8;
    private static final int WRITES_PER_THREAD = 1_000;
    private static final Instant T0 = Instant.parse("2026-10-17T10:00:00Z");

    @RepeatedTest(5)
    void concurrentWritesToOneCardAreNotLost() throws Exception {
        TransactionHistory history = new TransactionHistory(Duration.ofDays(1));

        runConcurrently(thread -> {
            for (int i = 0; i < WRITES_PER_THREAD; i++) {
                history.record("card-shared", T0.plusMillis(i));
            }
        });

        assertThat(history.countSince("card-shared", T0)).isEqualTo(THREADS * WRITES_PER_THREAD);
    }

    @Test
    void cardsAreCountedIndependentlyUnderConcurrency() throws Exception {
        TransactionHistory history = new TransactionHistory(Duration.ofDays(1));

        runConcurrently(thread -> {
            for (int i = 0; i < WRITES_PER_THREAD; i++) {
                history.record("card-" + thread, T0.plusMillis(i));
            }
        });

        for (int thread = 0; thread < THREADS; thread++) {
            assertThat(history.countSince("card-" + thread, T0)).isEqualTo(WRITES_PER_THREAD);
        }
    }

    @Test
    void readsStayWithinBoundsWhileWritesAreInFlight() throws Exception {
        TransactionHistory history = new TransactionHistory(Duration.ofDays(1));
        int writers = THREADS / 2;
        int maxCount = writers * WRITES_PER_THREAD;
        AtomicInteger writersRunning = new AtomicInteger(writers);

        runConcurrently(thread -> {
            if (thread < writers) {
                try {
                    for (int i = 0; i < WRITES_PER_THREAD; i++) {
                        history.record("card-rw", T0.plusMillis(i));
                    }
                } finally {
                    writersRunning.decrementAndGet();
                }
            } else {
                // Keep reading for as long as any writer is running, so reads overlap writes.
                // Also stop on interrupt, so shutdownNow() can end a reader if the test times out.
                while (writersRunning.get() > 0 && !Thread.currentThread().isInterrupted()) {
                    assertThat(history.countSince("card-rw", T0)).isBetween(0, maxCount);
                }
            }
        });

        assertThat(history.countSince("card-rw", T0)).isEqualTo(maxCount);
    }

    /**
     * Starts all workers together and rethrows the first failure from any of them. Stuck workers
     * are interrupted with {@code shutdownNow()} so a deadlock fails the test instead of hanging the build.
     */
    private static void runConcurrently(Worker worker) throws Exception {
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                int thread = t;
                futures.add(pool.submit(() -> {
                    start.await();
                    worker.run(thread);
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            pool.shutdownNow();
        }
    }

    @FunctionalInterface
    private interface Worker {
        void run(int thread) throws Exception;
    }
}
