package com.fs.pcmens2.deploy.cli.cmd;


import com.fs.pcmens2.deploy.core.*;
import com.fs.pcmens2.deploy.util.Prompt;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name="install", description = "Instala una versión de la plataforma (switch + restart + health)")
public class InstallerCmd implements Runnable {

    @Option(names="--version", description = "Versión a instalar (de releases/)")
    String version;

    @Option(names="--from", description = "Ruta a ZIP o carpeta del release")
    Path from;

    @Option(names="--force", description = "Sobrescribe si ya existe la versión en releases/")
    boolean force;

    @Option(names="--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PCM_DEPLOY_HOME")
    Path home;

    @Option(names="--yes", description = "No preguntar confirmación interactiva (modo no interactivo)")
    boolean yes;

    @Option(names="--manifest", description = "Ruta al manifest de la versión a instalar")
    Path manifest;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        version = resolveVersion(paths);

        // 1) Mostrar plan (dry-run) antes de activar
        ChecksumVerifier verifier = new ChecksumVerifier();
        Planner planner = new Planner(paths, verifier, new StateStore(paths));
        String plan = planner.renderPlan(version, manifest);
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
        installer.activate(version, manifest);
        System.out.println("Instalacion finalizada.");
    }

    private String resolveVersion(PlatformPaths paths) {
        boolean hasVersion = version != null && !version.isBlank();
        boolean hasFrom = from != null;
        if (hasVersion && hasFrom) {
            throw new IllegalArgumentException("Usa solo --version o --from, no ambos.");
        }
        if (!hasVersion && !hasFrom) {
            throw new IllegalArgumentException("Debes indicar --version o --from.");
        }
        if (hasFrom) {
            ReleaseStager stager = new ReleaseStager(paths);
            return stager.stageRelease(from, force).version();
        }
        return version;
    }
}
