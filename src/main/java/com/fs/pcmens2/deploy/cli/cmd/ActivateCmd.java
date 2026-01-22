package com.fs.pcmens2.deploy.cli.cmd;


import com.fs.pcmens2.deploy.core.*;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name="activate", description = "Activa una versión de la plataforma (switch + restart + health)")
public class ActivateCmd implements Runnable {

    @Option(names="--version", required = true, description = "Versión a activar (de releases/)")
    String version;

    @Option(names="--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PLATFORM_HOME")
    Path home;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        StateStore state = new StateStore(paths);
        SystemdService systemd = new SystemdService();
        HealthChecker healthChecker = new HealthChecker();
        ChecksumVerifier checksumVerifier = new ChecksumVerifier();
        Activator activator = new Activator(paths, state, systemd, healthChecker, checksumVerifier);
        activator.activate(version);
        System.out.println("Activación finalizada.");
    }
}

