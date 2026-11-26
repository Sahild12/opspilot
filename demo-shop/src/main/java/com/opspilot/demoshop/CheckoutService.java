package com.opspilot.demoshop;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

@Service
public class CheckoutService {

    private static final List<String> FAILURES = List.of(
            "Payment gateway timeout after 30000ms",
            "Database connection pool exhausted (active=20, max=20)",
            "NullPointerException in CartService.calculateTotal at line 87",
            "Inventory service returned HTTP 503 for sku=SHOE-42");

    private final Tracer tracer;
    private final ChaosState chaos;

    public CheckoutService(Tracer tracer, ChaosState chaos) {
        this.tracer = tracer;
        this.chaos = chaos;
    }

    /** Returns true if the order succeeded. */
    public boolean checkout() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        double failureRate = chaos.isEnabled() ? 0.70 : 0.02;

        Span span = tracer.spanBuilder("checkout").startSpan();
        try (Scope ignored = span.makeCurrent()) {
            if (rnd.nextDouble() < failureRate) {
                span.setStatus(StatusCode.ERROR, FAILURES.get(rnd.nextInt(FAILURES.size())));
                return false;
            }
            span.setAttribute("order.total", 10L + rnd.nextInt(190));
            return true;
        } finally {
            span.end();
        }
    }
}