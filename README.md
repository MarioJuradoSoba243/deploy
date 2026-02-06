# pcm-deploy - CLI de despliegue de PCM2

- Los servicios pueden declarar `commonLibs` en el manifest para copiar binarios adicionales al directorio de deploy.
    - `ReleaseStager.java` ? prepara releases desde ZIP o carpeta.
    - `pcm-deploy plan --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip`
    - `pcm-deploy install --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip`
2. `pcm-deploy install --version X.Y.Z` (Installer + HealthChecker).
3. En fallo, `pcm-deploy rollback` (Installer + StateStore).
- Validaciones: checksums SHA-256 y health checks HTTP; el sistema soporta rollback automático en fallo de activación.

Estructura relevante del código
- Comandos CLI: `bin/Main.java` y `cli/cmd/`:
    - `PlanCmd.java` ? genera un plan (dry-run).
    - `pcm-deploy plan --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip --force`
    - `pcm-deploy install --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip --force --yes`
1. `pcm-deploy plan --version X.Y.Z` o `pcm-deploy plan --from <release.zip>` (Planner).
2. `pcm-deploy install --version X.Y.Z` o `pcm-deploy install --from <release.zip>` (Installer + HealthChecker).
- Lógica central (`core/`):
    - `Planner.java` ? crea el plan de activación (servicios, copias, backups).
    - `Preparer.java` ? ejecuta pasos previos a la instalación.
    - `Installer.java` ? aplica cambios (copias, symlinks, control de `systemd`).
    - `ChecksumVerifier.java` ? verifica SHA-256 de artefactos.
    - `HealthChecker.java` ? realiza comprobaciones HTTP de salud.
    - `StateStore.java` ? mantiene estado (versión activa, paths).
    - `PlatformPaths.java` ? rutas estándar de plataforma/runtime.
    - `LockManager.java` ? evita ejecuciones concurrentes.
    - `FilesEx.java`, `Manifest.java`, `SystemdService.java` ? utilidades y modelos.
- Utilidades:
    - `util/Prompt.java` ? interacción y confirmaciones.

Comandos disponibles
- Plan (dry-run)
    - `pcm-deploy plan --version 2.4.0`
    - Muestra lo que se haría sin aplicar cambios (servicios a parar, copias, backups, health checks).
- Preparar release
    - `pcm-deploy prepare --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip`
    - Descomprime y valida checksums; prepara artefactos para instalación.
- Instalar / Activar
    - `pcm-deploy install --version 2.4.0`
    - Aplica el plan: para unidades `systemd`, backup de artefactos, copia de nuevos artefactos, actualización de symlinks (`platform/current` y `platform/previous`), arranque y health checks.
    - `--yes` para ejecución no interactiva.
- Rollback
    - `pcm-deploy rollback`
    - `pcm-deploy rollback --to 2.3.0`
    - Restaura la versión anterior si el installer determinó fallo o el usuario lo ordena.
- Estado
    - `pcm-deploy status`
    - Muestra versión activa, rutas y resultados de últimos steps.
- Verificar checksums
    - `pcm-deploy verify --version 2.4.0`
    - Usa `core/ChecksumVerifier.java`.
- Menú interactivo
    - `pcm-deploy menu`

Flujo típico
1. `pcm-deploy plan --version X.Y.Z` (Planner).
2. `pcm-deploy prepare --from <release>` (Preparer + ChecksumVerifier).
3. `pcm-deploy install --version X.Y.Z` (Installer + HealthChecker).
4. En fallo, `pcm-deploy rollback` (Installer + StateStore).

Validaciones y seguridad
- Checksums: SHA-256 (implementado en `core/ChecksumVerifier.java`).
- Health checks HTTP: comprobaciones configurables (implementado en `core/HealthChecker.java`).
- Locks para evitar ejecuciones concurrentes (`core/LockManager.java`).
- No se tocan datos ni logs; los backups de artefactos se almacenan en `runtime/backups/<timestamp>/`.

Compilar y ejecutar
- Compilar con Maven:
    - `mvn -DskipTests package`
- Ejecutar (jar exportado por el empaquetado):
    - `java -jar target/pcm-deploy-<version>.jar <comando> [opciones]`
- Alternativamente (desde código):
    - Ejecutar `bin/Main` como clase principal desde el IDE o con `mvn exec:java -Dexec.mainClass=com.fs.pcmens2.deploy.bin.Main`.

Archivos clave a revisar en el código fuente
- `src/main/java/com/fs/pcmens2/deploy/bin/Main.java`
- `src/main/java/com/fs/pcmens2/deploy/cli/cmd/*.java`
- `src/main/java/com/fs/pcmens2/deploy/core/*.java`
- `src/main/java/com/fs/pcmens2/deploy/util/Prompt.java`

Notas
- La herramienta está pensada para entornos Unix con `systemd`. Aunque se desarrolla en Windows/IntelliJ, la ejecución objetivo debe ser un sistema con `systemd`.
- El README omite ejemplos extensos; los comandos y responsabilidades están alineados con las clases del paquete `core` y `cli/cmd`.
