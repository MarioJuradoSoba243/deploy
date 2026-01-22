package com.fs.pcmens2.deploy.core;


import java.io.IOException;

public class SystemdService {

    public void stop(String unit) { run("systemctl", "stop", unit); }
    public void start(String unit) { run("systemctl", "start", unit); }
    public boolean isActive(String unit) { return run("systemctl", "is-active", unit) == 0; }

    int run(String... cmd) {
        try {
            var pb = new ProcessBuilder(cmd).redirectErrorStream(true);
            var p = pb.start();
            p.getInputStream().transferTo(System.out);
            return p.waitFor();
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}

