// src/main/java/com/example/digitalWalletDemo/controller/HealthCheckController.java

package com.example.digitalWalletDemo.controller.healthChecker;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST Controller for application health monitoring.
 * Demonstrates component scanning (@RestController) and endpoint mapping (@GetMapping).
 */
@RestController
@RequestMapping("/api")
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkHealth() {
        Map<String, Object> response = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now(),
                "service", "Digital Wallet API"
        );
        return ResponseEntity.ok(response);
    }
}