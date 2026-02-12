package com.finance.service;

import com.finance.dto.MarketQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Streams market quotes over SSE by polling Twelve Data at a fixed interval.
 */
@Service
public class MarketDataStreamService {

    private static final Logger log = LoggerFactory.getLogger(MarketDataStreamService.class);
    private static final long DEFAULT_TIMEOUT_MS = Duration.ofMinutes(10).toMillis();

    private final MarketDataClient marketDataClient;
    private final ScheduledExecutorService scheduler;

    public MarketDataStreamService(MarketDataClient marketDataClient) {
        this.marketDataClient = marketDataClient;
        this.scheduler = Executors.newScheduledThreadPool(2, new NamedThreadFactory("market-sse"));
    }

    public Optional<MarketQuote> getQuote(String symbol, String exchange) {
        return marketDataClient.getQuote(symbol, exchange);
    }

    /**
     * Starts an SSE stream that emits quotes for the requested symbols at the given interval.
     */
    public SseEmitter subscribe(List<String> symbols, String exchange, Duration interval) {
        Objects.requireNonNull(symbols, "symbols");
        Objects.requireNonNull(interval, "interval");

        // Clamp interval to avoid abuse
        long seconds = Math.max(2, Math.min(interval.toSeconds(), 30));
        Duration effectiveInterval = Duration.ofSeconds(seconds);

        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MS);

        // Send initial snapshot immediately
        scheduler.execute(() -> pushQuotes(emitter, symbols, exchange));

        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> pushQuotes(emitter, symbols, exchange),
                effectiveInterval.toSeconds(),
                effectiveInterval.toSeconds(),
                TimeUnit.SECONDS);

        emitter.onCompletion(() -> future.cancel(true));
        emitter.onTimeout(() -> {
            future.cancel(true);
            emitter.complete();
        });
        emitter.onError(ex -> {
            log.debug("SSE error, closing stream: {}", ex.getMessage());
            future.cancel(true);
        });

        return emitter;
    }

    private void pushQuotes(SseEmitter emitter, List<String> symbols, String exchange) {
        for (String symbol : symbols) {
            if (symbol == null || symbol.isBlank()) {
                continue;
            }
            try {
                marketDataClient.getQuote(symbol.trim(), exchange)
                        .ifPresent(quote -> sendQuote(emitter, quote));
            } catch (Exception ex) {
                log.warn("Failed to push quote for {}: {}", symbol, ex.getMessage());
            }
        }

        // heartbeat to keep connection alive if no data was sent
        try {
            emitter.send(SseEmitter.event()
                    .name("ping")
                    .id("ping-" + Instant.now().toEpochMilli())
                    .data("keep-alive", MediaType.TEXT_PLAIN));
        } catch (IOException ignored) {
            // ignore; emitter will close if truly broken
        }
    }

    private void sendQuote(SseEmitter emitter, MarketQuote quote) {
        try {
            emitter.send(SseEmitter.event()
                    .name("quote")
                    .id(quote.getSymbol() + "-" + Instant.now().toEpochMilli())
                    .data(quote, MediaType.APPLICATION_JSON));
        } catch (IOException ex) {
            log.debug("Emitter closed while sending {}: {}", quote.getSymbol(), ex.getMessage());
            emitter.completeWithError(ex);
        }
    }

    private static class NamedThreadFactory implements ThreadFactory {
        private final String baseName;
        private final AtomicInteger counter = new AtomicInteger(0);

        private NamedThreadFactory(String baseName) {
            this.baseName = baseName;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r);
            t.setName(baseName + "-" + counter.incrementAndGet());
            t.setDaemon(true);
            return t;
        }
    }
}
