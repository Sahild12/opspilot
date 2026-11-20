package com.opspilot.backend.service;

import java.time.Duration;
import java.time.Instant;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ErrorCounter {

    private static final Duration COUNTER_TTL = Duration.ofHours(2);
    private static final Duration SPIKE_COOLDOWN = Duration.ofMinutes(5);

    private final StringRedisTemplate redis;

    public ErrorCounter(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public static long minuteBucket(Instant t) {
        return t.getEpochSecond() / 60;
    }

    private String key(String service, long bucket) {
        return "errors:" + service + ":" + bucket;
    }

    public void recordError(String service) {
        String k = key(service, minuteBucket(Instant.now()));
        redis.opsForValue().increment(k);
        redis.expire(k, COUNTER_TTL);   // old counters clean themselves up
    }

    public long count(String service, long bucket) {
        String v = redis.opsForValue().get(key(service, bucket));
        return v == null ? 0 : Long.parseLong(v);
    }

    /** True only the first time within the cooldown window. */
    public boolean tryMarkSpike(String service) {
        Boolean first = redis.opsForValue()
                .setIfAbsent("spike:active:" + service, "1", SPIKE_COOLDOWN);
        return Boolean.TRUE.equals(first);
    }
}