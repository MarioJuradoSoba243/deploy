package com.fs.pcmens2.deploy.core;


import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Genera un "plan" (dry-run) textual de lo que ocurriría al activar una versión. */
public class Planner {
    private final PlatformPaths paths;
    private final ChecksumVerifier verifier;
    private final StateStore state;

    public Planner(PlatformPaths paths, ChecksumVerifier verifier, StateStore state) {
        this.paths = paths; this.verifier = verifier; this.state = state;
    }

    public String renderPlan(String version) {
        paths.ensureBaseDirs();
        Path release = paths.releaseDir(version);
        if (!Files.isDirectory(release)) throw new IllegalArgumentException("No existe releases/" + version);

        // Verificación de integridad
//        verifier.verifyRelease(release);
        verifier.listChecksums(release);
        var manifest = Manifest.load(release.resolve("manifest.yml"));

        var sb = new StringBuilder();
        var st = state.load();
        String current = st.currentVersion();
        String ts = Instant.now().toString().replace(":", "-");

        sb.append("\n=== pcm-deploy PLAN (dry-run) ===\n");
        sb.append("Plataforma: ").append(manifest.platform().name()).append("\n");
        sb.append("Versión objetivo: ").append(version).append("\n");
        sb.append("Versión activa actual: ").append(current == null ? "<ninguna>" : current).append("\n");
        sb.append("Release path: ").append(release).append("\n\n");

        // Orden por nombre para consistencia
        List<Manifest.ServiceDef> services = new ArrayList<>(manifest.services());
        services.sort(Comparator.comparing(Manifest.ServiceDef::name));

        sb.append("Servicios ("+services.size()+"): \n");
        for (var s : services) {
            Path src = release.resolve(s.artifact());
            Path dest = s.isJar()
                    ? Path.of(s.targetLibDir()).resolve(s.resolvedTargetFileName())
                    : Path.of(s.targetDeployPath());
            sb.append(String.format(" - %-12s type=%s\n", s.name(), s.type()));
            sb.append(String.format("     unit:    %s\n", s.systemdUnit()));
            sb.append(String.format("     source:  %s\n", src));
            sb.append(String.format("     target:  %s\n", dest));
            sb.append(String.format("     health:  %s\n", s.health().url()));
        }

        sb.append("\nAcciones previstas:\n");
        sb.append("  1) Parar unidades systemd (orden: manifest)\n");
        sb.append("  2) Backup de artefactos actuales -> ").append(paths.backupRoot()).append("/"+ts+"/<service>/\n");
        sb.append("  3) Copiar artefactos nuevos a destinos\n");
        sb.append("  4) Actualizar symlinks platform/current & previous\n");
        sb.append("  5) Arrancar unidades systemd\n");
        sb.append("  6) Health checks HTTP (/checkServlet)\n\n");

        sb.append("Unidades a gestionar:\n");
        for (var s : services) {
            sb.append(" - ").append(s.systemdUnit()).append("\n");
        }

        sb.append("\nRollback automático si algún servicio no supera el health check.\n");
        return sb.toString();
    }
}

