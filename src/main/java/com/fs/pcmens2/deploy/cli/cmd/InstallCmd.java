package com.fs.pcmens2.deploy.cli.cmd;


import picocli.CommandLine.*;
import com.fs.pcmens2.deploy.core.*;

import java.nio.file.Path;

@Command(name="install", description = "Instala (staging) una versión de plataforma desde ZIP o carpeta")
public class InstallCmd implements Runnable {

    @Option(names="--from", required = true, description = "Ruta a ZIP o carpeta del release")
    Path from;

    @Option(names="--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PLATFORM_HOME")
    Path home;

    @Option(names="--force", description = "Sobrescribe si ya existe la versión en releases/")
    boolean force;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        Installer installer = new Installer(paths, new ChecksumVerifier());
        installer.install(from, force);
        System.out.println("Instalación completada en: " + paths.releasesDir());
    }
}

