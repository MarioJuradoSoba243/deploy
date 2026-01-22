
# pcm-deploy ? CLI de despliegue local de PCM (Java 21)

**pcm-deploy** es una herramienta de línea de comandos para **desplegar localmente** la **plataforma PCM**, versionada como una **unidad completa** (una única versión para todos los artefactos). Está diseñada para máquinas Unix con **systemd**, **Java 21** y **Maven**.

> **Principios clave**
>
> - La **versión** es de **toda la plataforma PCM**, no por servicio.
> - Los **artefactos** (JAR/WAR) se traen a mano (ZIP o carpeta).
> - **No** se tocan **configuraciones, logs o datos** de los procesos.
> - Los **procesos ya existen** y se gestionan con **systemd**.
> - Para **JARs** se **copia** el artefacto al directorio `lib/` del servicio.
> - Para **WARs** se **copia** al directorio de despliegue (ej. `webapps/`).
> - Validación por **checksums SHA?256**, **health check HTTP** (`/checkServlet`) y **rollback automático**.

---

## Índice

1. Requisitos
2. Estructura de directorios
3. Formato del release PCM
4. Manifest de plataforma
5. Checksums
6. Comandos de la CLI
7. Plan (dry?run) y confirmación
8. Flujo de activación y rollback
9. Estado y symlinks
10. Instalación y compilación
11. Permisos y seguridad
12. Resolución de problemas

---

## Comandos de la CLI

```bash
# Plan (dry?run) de una activación
pcm-deploy plan --version 2.4.0

# Instalar un release
pcm-deploy install --from /opt/pcm-deploy/incoming/pcm-2.4.0.zip

# Activar una versión (pide confirmación; usa --yes para no interactivo)
pcm-deploy activate --version 2.4.0
pcm-deploy activate --version 2.4.0 --yes

# Rollback
pcm-deploy rollback
pcm-deploy rollback --to 2.3.0

# Estado
pcm-deploy status

# Verificar checksums
pcm-deploy verify --version 2.4.0

# Menú interactivo
pcm-deploy menu
```

---

## Plan (dry?run) y confirmación

- `plan` muestra **todo lo que ocurriría** al activar una versión: unidades a parar/arrancar, rutas de copia, backups previstos y health checks. **No aplica cambios.**
- `activate` imprime el **plan** y solicita **confirmación interactiva**. Usa `--yes` para ejecución no interactiva (por ejemplo, desde un script).

Ejemplo de salida abreviada:

```
=== pcm-deploy PLAN (dry-run) ===
Plataforma: PCM
Versión objetivo: 2.4.0
Versión activa actual: 2.3.0

Servicios (2):
 - users        type=jar
     unit:    pcm-users.service
     source:  /opt/pcm-deploy/platform/releases/2.4.0/artifacts/users.jar
     target:  /srv/users/lib/users.jar
     health:  http://localhost:8081/checkServlet
 - web          type=war
     unit:    tomcat.service
     source:  /opt/pcm-deploy/platform/releases/2.4.0/artifacts/web.war
     target:  /opt/tomcat/webapps/web.war
     health:  http://localhost:8080/web/checkServlet

Acciones previstas:
  1) Parar unidades systemd
  2) Backup de artefactos actuales -> /opt/pcm-deploy/runtime/backups/<timestamp>/<service>/
  3) Copiar artefactos nuevos a destinos
  4) Actualizar symlinks platform/current & previous
  5) Arrancar unidades systemd
  6) Health checks HTTP (/checkServlet)
```

---

(El resto de secciones se mantienen como en la versión previa del README.)
