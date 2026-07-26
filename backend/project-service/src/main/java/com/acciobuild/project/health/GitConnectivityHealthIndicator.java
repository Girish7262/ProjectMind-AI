package com.acciobuild.project.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Custom Actuator Health Indicator verifying remote Git platform connectivity.
 */
@Component
public class GitConnectivityHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // Check internet/network connectivity by pinging Github APIs
            URL url = new URL("https://api.github.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();
            
            int code = connection.getResponseCode();
            if (code == 200) {
                return Health.up()
                        .withDetail("ping_target", "https://api.github.com")
                        .withDetail("status", "Git provider connection established successfully.")
                        .build();
            }
            return Health.status("DEGRADED")
                    .withDetail("status_code", code)
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("message", "GitHub API unreachable. Connected projects syncs might fail.")
                    .build();
        }
    }
}
