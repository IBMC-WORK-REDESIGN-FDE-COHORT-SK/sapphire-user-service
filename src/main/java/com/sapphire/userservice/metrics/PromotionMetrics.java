package com.sapphire.userservice.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Wraps Micrometer counters for promotion engagement events.
 * All increment methods are intentionally non-throwing — callers
 * must handle exceptions if they matter (FR-018).
 */
@Component
public class PromotionMetrics {

    private static final String METRIC_PREFIX = "sapphire.promotion";

    private final MeterRegistry meterRegistry;

    public PromotionMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void incrementImpressionCounter(String tier) {
        counter("impressions", tier).increment();
    }

    public void incrementClickCounter(String tier) {
        counter("clicks", tier).increment();
    }

    public void incrementDismissCounter(String tier) {
        counter("dismissals", tier).increment();
    }

    private Counter counter(String event, String tier) {
        return Counter.builder(METRIC_PREFIX + "." + event)
                .tag("tier", tier)
                .register(meterRegistry);
    }
}
