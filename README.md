# pcm-deploy - CLI de despliegue de PCM2

pcm-deploy es una herramienta de línea de comandos para desplegar localmente la plataforma PCM en entornos Unix con `systemd`, Java 21 y Maven. Permite planificar, activar y hacer rollback de versiones de la plataforma a partir de artefactos (JAR/WAR) empaquetados en ZIP o en una carpeta.
## Puntos relevantes
- La versión es de toda la plataforma PCM (unidad completa).
- Validaciones: checksums SHA-256 y health checks HTTP; el sistema soporta rollback automático en fallo de activación.

## Comandos disponibles (con ejemplos)
### Plan (dry-run)
Muestra el plan de activación sin aplicar cambios.
- `pcm-deploy plan --version 2.4.0`
- `pcm-deploy plan --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip`
- `pcm-deploy plan --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip --force`

### Instalar / Activar
Activa una versión: detiene servicios, realiza backups, copia artefactos, actualiza symlinks y arranca con health checks.
- `pcm-deploy install --version 2.4.0`
- `pcm-deploy install --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip`
- `pcm-deploy install --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip --force --yes`

### Rollback
Vuelve a la versión previa o a una versión concreta.
- `pcm-deploy rollback`
- `pcm-deploy rollback --to 2.3.0`

### Estado
Muestra versión activa, previa y estado de `systemd`.
- `pcm-deploy status`

### Menú interactivo
Muestra un menú con accesos directos a comandos.
- `pcm-deploy menu`

## Flujo típico
1. `pcm-deploy plan --version X.Y.Z` o `pcm-deploy plan --from <release.zip>`.
2. `pcm-deploy install --version X.Y.Z` o `pcm-deploy install --from <release.zip>`.
3. En fallo, `pcm-deploy rollback`.

## Compilar y ejecutar
- Alternativamente (desde código):
## Estructura del código
- Comandos CLI: `src/main/java/com/fs/pcmens2/deploy/cli/cmd/`
- Lógica central: `src/main/java/com/fs/pcmens2/deploy/core/`
- Utilidades: `src/main/java/com/fs/pcmens2/deploy/util/`
## Notas
- La herramienta está pensada para entornos Unix con `systemd`.
- El staging de releases se integra en `plan`/`install` cuando se usa `--from`.
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
