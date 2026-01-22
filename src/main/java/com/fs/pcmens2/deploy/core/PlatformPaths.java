package com.fs.pcmens2.deploy.core;


import java.io.IOException;
import java.nio.file.*;
import static java.nio.file.StandardCopyOption.*;

public class PlatformPaths {
    private final Path home;

    private PlatformPaths(Path home) {
        this.home = home;
    }

    public static PlatformPaths of(java.nio.file.Path override) {
        var base = override != null
                ? override
                : Path.of(System.getenv().getOrDefault("PLATFORM_HOME", "/opt/pcm-deploy"));
        return new PlatformPaths(base.toAbsolutePath().normalize());
    }

    public Path home() { return home; }
    public Path incomingDir() { return home.resolve("incoming"); }
    public Path platformDir() { return home.resolve("platform"); }
    public Path releasesDir() { return platformDir().resolve("releases"); }
    public Path releaseDir(String version) { return releasesDir().resolve(version); }
    public Path currentSymlink() { return platformDir().resolve("current"); }
    public Path previousSymlink() { return platformDir().resolve("previous"); }
    public Path currentReleaseDir() { return resolveSymlink(currentSymlink()); }

    public Path stateDir() { return home.resolve("state"); }
    public Path stateFile() { return stateDir().resolve("state.json"); }
    public Path lockFile() { return stateDir().resolve("deploy.lock"); }

    public Path runtimeDir() { return home.resolve("runtime"); }
    public Path backupRoot() { return runtimeDir().resolve("backups"); }

    public void ensureBaseDirs() {
        try {
            Files.createDirectories(incomingDir());
            Files.createDirectories(releasesDir());
            Files.createDirectories(stateDir());
            Files.createDirectories(backupRoot());
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public void switchCurrent(String newVersion) {
        try {
            Path newTarget = releaseDir(newVersion);
            if (!Files.isDirectory(newTarget)) throw new IllegalArgumentException("No existe release " + newVersion);

            // actualizar previous -> current
            if (Files.isSymbolicLink(currentSymlink())) {
                Path currTarget = Files.readSymbolicLink(currentSymlink());
                recreateSymlink(previousSymlink(), currTarget);
            }
            // current -> newVersion
            recreateSymlink(currentSymlink(), home.relativize(newTarget));
        } catch (IOException e) {
            throw new RuntimeException("Error al actualizar symlinks", e);
        }
    }

    public void switchToPrevious() {
        try {
            if (!Files.isSymbolicLink(previousSymlink()))
                throw new IllegalStateException("No hay previous");
            Path prevTarget = Files.readSymbolicLink(previousSymlink());
            recreateSymlink(currentSymlink(), prevTarget);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    private static void recreateSymlink(Path link, Path target) throws IOException {
        Files.deleteIfExists(link);
        Files.createSymbolicLink(link, target);
    }

    private static Path resolveSymlink(Path link) {
        try {
            return Files.isSymbolicLink(link) ? link.getParent().resolve(Files.readSymbolicLink(link)).normalize() : link;
        } catch (IOException e) { throw new RuntimeException(e); }
    }
}

