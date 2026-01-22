package com.fs.pcmens2.deploy.cli.cmd;



import picocli.CommandLine.*;
import com.fs.pcmens2.deploy.core.*;

import java.nio.file.Path;

@Command(name="verify", description = "Verifica los checksums de una versión (en releases/)")
public class VerifyCmd implements Runnable {

    @Option(names="--version", required = true, description = "Versión a verificar")
    String version;

    @Option(names="--home", description = "Directorio base. Por defecto: /opt/pcm-deploy o $PLATFORM_HOME")
    Path home;

    @Override public void run() {
        PlatformPaths paths = PlatformPaths.of(home);
        ChecksumVerifier verifier = new ChecksumVerifier();
        verifier.verifyRelease(paths.releaseDir(version));
        System.out.println("Checksums OK para versión " + version);
    }
}
