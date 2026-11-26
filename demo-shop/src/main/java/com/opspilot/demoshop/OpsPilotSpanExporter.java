package com.opspilot.demoshop;

import java.util.Collection;

import org.springframework.stereotype.Component;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

@Component
public class OpsPilotSpanExporter implements SpanExporter {

    private final OpsPilotClient opsPilot;

    public OpsPilotSpanExporter(OpsPilotClient opsPilot) {
        this.opsPilot = opsPilot;
    }

    @Override
    public CompletableResultCode export(Collection<SpanData> spans) {
        for (SpanData span : spans) {
            boolean failed = span.getStatus().getStatusCode() == StatusCode.ERROR;
            if (failed) {
                opsPilot.send("ERROR",
                        "Checkout failed: " + span.getStatus().getDescription(),
                        span.getTraceId());
            } else {
                Long total = span.getAttributes().get(AttributeKey.longKey("order.total"));
                opsPilot.send("INFO", "Order placed successfully, total=" + total, span.getTraceId());
            }
        }
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode flush() {
        return CompletableResultCode.ofSuccess();
    }

    @Override
    public CompletableResultCode shutdown() {
        return CompletableResultCode.ofSuccess();
    }
}