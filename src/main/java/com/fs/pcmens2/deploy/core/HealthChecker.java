package com.fs.pcmens2.deploy.core;



import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class HealthChecker {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    /**
     * Indica si el servicio tiene un health check HTTP configurado.
     *
     * @param svc definición del servicio
     * @return true si existe URL de health, en otro caso false
     */
    public static boolean hasHealthCheck(Manifest.ServiceDef svc) {
        if (svc == null || svc.health() == null) {
            return false;
        }
        String url = svc.health().url();
        return url != null && !url.isBlank();
    }

    public boolean waitUntilHealthy(Manifest.ServiceDef svc, Duration overallTimeout) {
        if (!hasHealthCheck(svc)) {
            return true;
        }
        String url = svc.health().url();
        Duration timeoutPerTry = Duration.ofSeconds(svc.health().timeoutSecondsOrDefault());
        int retries = svc.health().retriesOrDefault();
        long deadline = System.nanoTime() + overallTimeout.toNanos();

        for (int i = 0; i <= retries; i++) {
            if (System.nanoTime() > deadline) return false;
            if (check(url, timeoutPerTry)) return true;
            try { Thread.sleep(Math.min(2000, timeoutPerTry.toMillis())); } catch (InterruptedException ignored) {}
        }
        return false;
    }

    private boolean check(String url, Duration timeout) {
        try {
            HttpRequest req = HttpRequest.newBuilder(URI.create(url)).timeout(timeout).GET().build();
            HttpResponse<Void> res = client.send(req, HttpResponse.BodyHandlers.discarding());
            return res.statusCode() == 200;
        } catch (Exception e) { return false; }
    }
}
