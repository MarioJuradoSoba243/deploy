package com.fs.pcmens2.deploy.cli.cmd;



import com.fs.pcmens2.deploy.core.*;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name = "plan", description = "Muestra un plan (dry-run) de la activación de una versión")
public class PlanCmd implements Runnable {

    @Option(names = "--version", description = "Versión a planificar")
    String version;

    @Option(names="--from", description = "Ruta a ZIP o carpeta del release")
    Path from;

    @Option(names="--force", description = "Sobrescribe si ya existe la versión en releases/")
    boolean force;

    @Option(names = "--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PCM_DEPLOY_HOME")
    Path home;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        version = resolveVersion(paths);
        ChecksumVerifier verifier = new ChecksumVerifier();
        Planner planner = new Planner(paths, verifier, new StateStore(paths));
        System.out.println(planner.renderPlan(version));
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
