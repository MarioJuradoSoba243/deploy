package com.fs.pcmens2.deploy.cli.cmd;


import com.fs.pcmens2.deploy.core.*;
import picocli.CommandLine.*;

import java.nio.file.Path;

@Command(name="rollback", description = "Vuelve a la versión previa o a una versión concreta")
public class RollbackCmd implements Runnable {

    @Option(names = "--to", description = "Versión concreta a la que volver (opcional)")
    String toVersion;

    @Option(names = "--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PCM_DEPLOY_HOME")
    Path home;

    @Override
    public void run() {
        var paths = PlatformPaths.of(home);
        var state = new StateStore(paths);
        var systemd = new SystemdService();
        var checker = new HealthChecker();
        var checksumVerifier = new ChecksumVerifier();

        Installer installer = new Installer(paths, state, systemd, checker, checksumVerifier);
        installer.rollback(toVersion);
    }
}
