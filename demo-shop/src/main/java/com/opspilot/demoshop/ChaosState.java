package com.opspilot.demoshop;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Component;

@Component
public class ChaosState {
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    public boolean isEnabled() { return enabled.get(); }
    public void set(boolean value) { enabled.set(value); }
}