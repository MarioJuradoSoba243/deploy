package com.fs.pcmens2.deploy.cli.cmd;



import com.fs.pcmens2.deploy.core.*;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name = "plan", description = "Muestra un plan (dry-run) de la activación de una versión")
public class PlanCmd implements Runnable {

    @Option(names = "--version", required = true, description = "Versión a planificar")
    String version;

    @Option(names = "--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PCM_DEPLOY_HOME")
    Path home;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        ChecksumVerifier verifier = new ChecksumVerifier();
        Planner planner = new Planner(paths, verifier, new StateStore(paths));
        System.out.println(planner.renderPlan(version));
    }
}

