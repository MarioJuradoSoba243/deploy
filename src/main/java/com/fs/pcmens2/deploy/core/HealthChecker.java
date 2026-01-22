package com.fs.pcmens2.deploy.core;



import java.net.URI;
import java.net.http.*;
import java.time.Duration;

public class HealthChecker {
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public boolean waitUntilHealthy(Manifest.ServiceDef svc, Duration overallTimeout) {
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

