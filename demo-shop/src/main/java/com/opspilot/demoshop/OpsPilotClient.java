package com.opspilot.demoshop;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class OpsPilotClient {

    private final RestClient client;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public OpsPilotClient(@Value("${opspilot.url}") String url) {
        this.client = RestClient.builder().baseUrl(url).build();
    }

    public void send(String level, String message, String traceId) {
        Map<String, Object> body = Map.of(
                "service", "demo-shop",
                "level", level,
                "message", message,
                "traceId", traceId,
                "timestamp", Instant.now().toString());

        // Fire and forget on a background thread. If OpsPilot is down, the shop must not break.
        executor.submit(() -> {
            try {
                client.post().uri("/api/logs").body(body).retrieve().toBodilessEntity();
            } catch (Exception e) {
                System.err.println("Could not reach OpsPilot: " + e.getMessage());
            }
        });
    }
}