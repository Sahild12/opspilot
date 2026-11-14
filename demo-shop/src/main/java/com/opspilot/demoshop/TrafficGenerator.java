package com.opspilot.demoshop;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TrafficGenerator {

    private final CheckoutService checkoutService;

    public TrafficGenerator(CheckoutService checkoutService) {
        this.checkoutService = checkoutService;
    }

    // Two fake customers per second
    @Scheduled(fixedRate = 500)
    public void simulateCustomer() {
        checkoutService.checkout();
    }
}