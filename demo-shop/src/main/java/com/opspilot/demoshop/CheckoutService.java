package com.opspilot.demoshop;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

@Service
public class CheckoutService {

    private static final List<String> FAILURES = List.of(
            "Payment gateway timeout after 30000ms",
            "Database connection pool exhausted (active=20, max=20)",
            "NullPointerException in CartService.calculateTotal at line 87",
            "Inventory service returned HTTP 503 for sku=SHOE-42");

    private final OpsPilotClient opsPilot;
    private final ChaosState chaos;

    public CheckoutService(OpsPilotClient opsPilot, ChaosState chaos) {
        this.opsPilot = opsPilot;
        this.chaos = chaos;
    }

    /** Returns true if the order succeeded. */
    public boolean checkout() {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        // Normal days have a tiny error rate (2%). During chaos, 70% of orders fail.
        double failureRate = chaos.isEnabled() ? 0.70 : 0.02;

        if (rnd.nextDouble() < failureRate) {
            String reason = FAILURES.get(rnd.nextInt(FAILURES.size()));
            opsPilot.send("ERROR", "Checkout failed: " + reason, traceId);
            return false;
        }

        opsPilot.send("INFO", "Order placed successfully, total=" + (10 + rnd.nextInt(190)), traceId);
        return true;
    }
}