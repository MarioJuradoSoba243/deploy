package com.fs.pcmens2.deploy.cli.cmd;


import com.fs.pcmens2.deploy.core.*;
import com.fs.pcmens2.deploy.util.Prompt;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name="install", description = "Instala una versión de la plataforma (switch + restart + health)")
public class InstallerCmd implements Runnable {

    @Option(names="--version", required = true, description = "Versión a instalar (de releases/)")
    String version;

    @Option(names="--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PCM_DEPLOY_HOME")
    Path home;

    @Option(names="--yes", description = "No preguntar confirmación interactiva (modo no interactivo)")
    boolean yes;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);

        // 1) Mostrar plan (dry-run) antes de activar
        ChecksumVerifier verifier = new ChecksumVerifier();
        Planner planner = new Planner(paths, verifier, new StateStore(paths));
        String plan = planner.renderPlan(version);
        System.out.println(plan);

        // 2) Confirmación interactiva (omitible con --yes)
        if (!yes) {
            boolean ok = Prompt.confirm("¿Deseas continuar con la instalacion de la versión " + version + "?", true);
            if (!ok) {
                System.out.println("Operación cancelada por el usuario.");
                return;
            }
        }

        // 3) Activación real
        StateStore state = new StateStore(paths);
        SystemdService systemd = new SystemdService();
        HealthChecker checker = new HealthChecker();
        Installer installer = new Installer(paths, state, systemd, checker, verifier);
        installer.activate(version);
        System.out.println("Instalacion finalizada.");
    }
}


