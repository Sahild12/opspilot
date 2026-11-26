package com.opspilot.demoshop;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

@Configuration
public class OtelConfig {

    @Bean(destroyMethod = "close")
    public OpenTelemetrySdk openTelemetry(OpsPilotSpanExporter exporter) {
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(exporter)
                        .setScheduleDelay(Duration.ofMillis(500))
                        .build())
                .build();
        return OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
    }

    @Bean
    public Tracer tracer(OpenTelemetrySdk openTelemetry) {
        return openTelemetry.getTracer("demo-shop");
    }
}