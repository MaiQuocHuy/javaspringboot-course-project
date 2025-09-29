package project.ktc.springboot_app.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Simple health controller to handle root /health endpoint requests
 * and delegate to Spring Boot Actuator's health endpoint
 */
@RestController
public class HealthController {

    @Autowired
    private HealthEndpoint healthEndpoint;

    /**
     * Health check endpoint that deployment platforms typically expect at /health
     * This delegates to the Spring Boot Actuator health endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<HealthComponent> health() {
        HealthComponent health = healthEndpoint.health();
        return ResponseEntity.ok(health);
    }
}
