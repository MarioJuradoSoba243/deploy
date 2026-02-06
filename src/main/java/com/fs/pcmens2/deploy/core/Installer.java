package com.fs.pcmens2.deploy.core;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.*;

public class Installer {

    private final PlatformPaths paths;
    private final StateStore state;
    private final SystemdService systemd;
    private final HealthChecker health;
    private final ChecksumVerifier verifier;

    public Installer(PlatformPaths paths, StateStore state, SystemdService systemd, HealthChecker health, ChecksumVerifier verifier) {
        this.paths = paths; this.state = state; this.systemd = systemd; this.health = health; this.verifier = verifier;
    }

    public void activate(String version) {
        paths.ensureBaseDirs();
        try (LockManager lock = new LockManager(paths)) {
            Path release = paths.releaseDir(version);
            if (!Files.isDirectory(release)) throw new IllegalArgumentException("No existe releases/" + version);

            // Genera checksums
//            verifier.verifyRelease(release);
            verifier.generateChecksums(release);

            Manifest manifest = Manifest.load(release.resolve("manifest.yml"));

            String current = state.load().currentVersion();
            System.out.println("Activando versión " + version + " (previa: " + current + ")");

            // STOP all
            for (Manifest.ServiceDef svc : manifest.services()) {
                System.out.println("Parando " + svc.systemdUnit());
                systemd.stop(svc.systemdUnit());
            }

            // BACKUP current artifacts
            List<Backup> backups = backupCurrentArtifacts(manifest);

            try {
                // COPY new artifacts
                copyArtifactsFromRelease(release, manifest);

                // SWITCH symlink (platform-wide)
                paths.switchCurrent(version);

                // START all
                for (Manifest.ServiceDef svc : manifest.services()) {
                    System.out.println("Arrancando " + svc.systemdUnit());
                    systemd.start(svc.systemdUnit());
                }

                // HEALTH
                for (Manifest.ServiceDef svc : manifest.services()) {
                    System.out.println("Health check: " + svc.name() + " -> " + svc.health().url());
                    if (!health.waitUntilHealthy(svc, Duration.ofSeconds(60))) {
                        throw new IllegalStateException("Health FAILED para " + svc.name());
                    }
                }

                // STATE
                state.commitActivation(version, current);
                System.out.println("Activación OK: " + version);

            } catch (Exception ex) {
                System.err.println("Fallo en activación: " + ex.getMessage() + " -> iniciando rollback automático");
                // ROLLBACK
                for (Manifest.ServiceDef svc : manifest.services()) systemd.stop(svc.systemdUnit());
                restoreBackups(backups);
                if (current != null) {
                    paths.switchToPrevious(); // previous apunta al current previo
                    // Arrancar servicios previos (leer manifest del previous)
                    Manifest prevManifest = Manifest.load(paths.currentReleaseDir().resolve("manifest.yml"));
                    for (Manifest.ServiceDef svc : prevManifest.services()) systemd.start(svc.systemdUnit());
                }
                state.markFailed(current, version, ex.getMessage());
                throw new RuntimeException("Rollback aplicado. Plataforma restaurada.", ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void rollback(String toVersion) {
        paths.ensureBaseDirs();
        try (LockManager lock = new LockManager(paths)) {
            StateStore.State st = state.load();
            String target = toVersion != null ? toVersion : st.previousVersion();
            if (target == null) throw new IllegalStateException("No hay previous ni --to especificado.");
            System.out.println("Rollback a versión " + target);
            activate(target); // reutiliza la misma lógica de activación para coherencia
        }
    }

    private record Backup(Path original, Path backup) {}

    private List<Backup> backupCurrentArtifacts(Manifest manifest) throws IOException {
        List<Backup> list = new ArrayList<>();
        String ts = Instant.now().toString().replace(":", "-");
        Path root = paths.backupRoot().resolve(ts);
        for (Manifest.ServiceDef s : manifest.services()) {
            Path orig = s.isJar()
                    ? Path.of(s.targetLibDir()).resolve(s.resolvedTargetFileName())
                    : Path.of(s.targetDeployPath());
            if (Files.exists(orig)) {
                Path bak = root.resolve(s.name()).resolve(orig.getFileName());
                Files.createDirectories(bak.getParent());
                Files.move(orig, bak, REPLACE_EXISTING, ATOMIC_MOVE);
                list.add(new Backup(orig, bak));
                System.out.println("Backup " + orig + " -> " + bak);
            } else {
                System.out.println("No existe actual para " + s.name() + " (" + orig + "), no se hace backup");
            }
        }
        return list;
    }

    private void restoreBackups(List<Backup> list) {
        for (Backup b : list) {
            try {
                Files.createDirectories(b.original().getParent());
                Files.move(b.backup(), b.original(), REPLACE_EXISTING, ATOMIC_MOVE);
                System.out.println("Restaurado " + b.backup() + " -> " + b.original());
            } catch (Exception e) {
                System.err.println("Error restaurando " + b.backup() + ": " + e.getMessage());
            }
        }
    }

    private void copyArtifactsFromRelease(Path release, Manifest manifest) throws IOException {
        for (Manifest.ServiceDef s : manifest.services()) {
            Path src = release.resolve(s.artifact());
            if (!Files.exists(src)) throw new IllegalStateException("No existe artefacto: " + src);
            if (s.isJar()) {
                Path dest = Path.of(s.targetLibDir()).resolve(s.resolvedTargetFileName());
                Files.createDirectories(dest.getParent());
                Files.copy(src, dest, REPLACE_EXISTING);
                System.out.println("Copiado JAR: " + src + " -> " + dest);
            } else if (s.isWar()) {
                Path dest = Path.of(s.targetDeployPath());
                Files.createDirectories(dest.getParent());
                Files.copy(src, dest, REPLACE_EXISTING);
                System.out.println("Copiado WAR: " + src + " -> " + dest);
            } else {
                throw new IllegalArgumentException("Tipo no soportado: " + s.type());
            }
        }
    }
}
