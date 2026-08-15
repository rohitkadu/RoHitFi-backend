package com.rohitfi.config;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sanity-check endpoint. Hit GET /api/health after first run to confirm
 * the app booted and (once wired up) that Postgres/Mongo are reachable.
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "service", "RoHitFi Backend"
        );
    }
}
