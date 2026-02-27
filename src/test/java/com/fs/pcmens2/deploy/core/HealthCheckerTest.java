package com.fs.pcmens2.deploy.core;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HealthCheckerTest {

    @Test
    void hasHealthCheckReturnsFalseWhenHealthMissing() {
        Manifest.ServiceDef service = buildService(null);

        assertFalse(HealthChecker.hasHealthCheck(service));
    }

    @Test
    void hasHealthCheckReturnsFalseWhenUrlBlank() {
        Manifest.ServiceDef service = buildService(new Manifest.Health("  ", null, null));

        assertFalse(HealthChecker.hasHealthCheck(service));
    }

    @Test
    void waitUntilHealthyReturnsTrueWhenNoHealthConfigured() {
        Manifest.ServiceDef service = buildService(null);
        HealthChecker checker = new HealthChecker();

        assertTrue(checker.waitUntilHealthy(service, Duration.ofSeconds(1)));
    }

    private Manifest.ServiceDef buildService(Manifest.Health health) {
        return new Manifest.ServiceDef(
                "svc",
                "jar",
                "svc.jar",
                List.of(),
                "/opt/pcm/lib",
                "/opt/pcm/webapps/svc.war",
                "svc.service",
                health,
                null
        );
    }
}
