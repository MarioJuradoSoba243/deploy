package com.fs.pcmens2.deploy.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ReleaseStager {
    private final PlatformPaths paths;

    public ReleaseStager(PlatformPaths paths) {
        this.paths = paths;
    }

    public StagedRelease stageRelease(Path from, boolean force) {
        paths.ensureBaseDirs();
        if (from == null) throw new IllegalArgumentException("Ruta de release requerida (--from).");

        Path temp = null;
        boolean cleanup = false;
        try {
            if (!Files.exists(from)) throw new IllegalArgumentException("No existe: " + from);
            if (Files.isDirectory(from)) {
                temp = from.toAbsolutePath().normalize();
            } else {
                temp = Files.createTempDirectory("platform-release-");
                cleanup = true;
                unzip(from, temp);
            }

            Path releaseRoot = detectReleaseRoot(temp);
            Manifest manifest = Manifest.load(releaseRoot.resolve("manifest.yml"));
            String version = manifest.platform().version();
            if (version == null || version.isBlank()) {
                throw new IllegalStateException("Manifest sin versión de plataforma.");
            }

            Path target = paths.releaseDir(version);
            if (Files.exists(target)) {
                if (!force) {
                    throw new IllegalStateException("Ya existe releases/" + version + " (usa --force para sobrescribir)");
                }
                FilesEx.deleteRecursively(target);
            }
            Files.createDirectories(target.getParent());
            FilesEx.copyRecursively(releaseRoot, target);

            StateStore.State state = new StateStore(paths).load();
            state = state.withInstalled(version);
            new StateStore(paths).save(state);

            return new StagedRelease(version, target);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (cleanup && temp != null) {
                try {
                    FilesEx.deleteRecursively(temp);
                } catch (IOException e) {
                    throw new RuntimeException("No se pudo limpiar staging temporal: " + temp, e);
                }
            }
        }
    }

    private Path detectReleaseRoot(Path temp) {
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

    public record StagedRelease(String version, Path releaseDir) {}
}
