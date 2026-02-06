package com.fs.pcmens2.deploy.core;


import java.io.IOException;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Preparer {

    private final PlatformPaths paths;
    private final ChecksumVerifier verifier;

    public Preparer(PlatformPaths paths, ChecksumVerifier verifier) {
        this.paths = paths; this.verifier = verifier;
    }

    public void install(Path from, boolean force) {
        paths.ensureBaseDirs();

        // Extraer/ubicar staging temporal
        Path temp;
        try {
            if (!Files.exists(from)) throw new IllegalArgumentException("No existe: " + from);
            if (Files.isDirectory(from)) {
                temp = from.toAbsolutePath().normalize();
            } else {
                temp = Files.createTempDirectory("platform-release-");
                unzip(from, temp);
            }
            // detectar carpeta raíz (puede venir platform/.... o directamente)
            Path releaseRoot = detectReleaseRoot(temp);
            Manifest manifest = Manifest.load(releaseRoot.resolve("manifest.yml"));
            String version = manifest.platform().version();
            Path target = paths.releaseDir(version);

            if (Files.exists(target)) {
                if (!force) throw new IllegalStateException("Ya existe releases/" + version + " (usa --force para sobrescribir)");
                FilesEx.deleteRecursively(target);
            }
            Files.createDirectories(target.getParent());
            FilesEx.copyRecursively(releaseRoot, target);

            // verificar checksums

            verifier.listChecksums(target);

            // registrar en state
            StateStore.State state = new StateStore(paths).load();
            state = state.withInstalled(manifest.platform().version());
            new StateStore(paths).save(state);

            System.out.println("Release " + version + " preparada para instalar en " + target);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private Path detectReleaseRoot(Path temp) {
        // si hay subcarpeta "platform" úsala, si no temp
        Path maybe = temp.resolve("platform");
        if (Files.isDirectory(maybe)) return maybe;
        return temp;
    }

    private void unzip(Path zip, Path destDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path target = destDir.resolve(entry.getName()).normalize();
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                } else {
                    Files.createDirectories(target.getParent());
                    Files.copy(zis, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
