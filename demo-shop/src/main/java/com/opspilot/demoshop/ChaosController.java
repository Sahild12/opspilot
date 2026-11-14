package com.opspilot.demoshop;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/chaos")
public class ChaosController {

    private final ChaosState chaos;

    public ChaosController(ChaosState chaos) { this.chaos = chaos; }

    @GetMapping
    public Map<String, Boolean> status() { return Map.of("chaos", chaos.isEnabled()); }

    @PostMapping("/on")
    public Map<String, Boolean> on() { chaos.set(true); return status(); }

    @PostMapping("/off")
    public Map<String, Boolean> off() { chaos.set(false); return status(); }
}