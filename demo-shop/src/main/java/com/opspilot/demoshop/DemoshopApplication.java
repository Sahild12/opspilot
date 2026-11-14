package com.opspilot.demoshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DemoshopApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoshopApplication.class, args);
    }
}