package com.fs.pcmens2.deploy.cli.cmd;

import picocli.CommandLine.*;
import com.fs.pcmens2.deploy.core.*;

import java.nio.file.Path;

@Command(name="status", description = "Muestra estado actual, version activa y systemd")
public class StatusCmd implements Runnable {

    @Option(names="--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PCM_DEPLOY_HOME")
    Path home;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        StateStore state = new StateStore(paths);
        SystemdService systemd = new SystemdService();

        StateStore.State st = state.load();
        System.out.println("Versión activa: " + st.currentVersion());
        System.out.println("Versión previa: " + st.previousVersion());
        System.out.println("Preparadas: " + st.installedVersions());

        Manifest manifest = Manifest.load(paths.currentReleaseDir().resolve("manifest.yml"));

        System.out.println("\nServicios:");
        for (var svc : manifest.services()) {
            boolean active = systemd.isActive(svc.systemdUnit());
            System.out.printf(" - %-12s systemd=%s healthURL=%s%n",
                    svc.name(), active ? "active" : "inactive",
                    svc.health().url());
        }
    }
}
