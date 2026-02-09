package com.fs.pcmens2.deploy.core;

import java.nio.file.Path;

/**
 * Resuelve la ruta del manifest a usar para una release concreta.
 */
public final class ManifestResolver {

    private ManifestResolver() {
    }

    /**
     * Devuelve la ruta del manifest, usando una ruta explícita si se proporciona.
     *
     * @param releaseDir la carpeta de la release seleccionada
     * @param manifestOverride la ruta del manifest indicada por el usuario, si existe
     * @return ruta absoluta o relativa al manifest a cargar
     */
    public static Path resolve(Path releaseDir, Path manifestOverride) {
        if (manifestOverride == null) {
            return releaseDir.resolve("manifest.yml");
        }
        if (manifestOverride.isAbsolute()) {
            return manifestOverride;
        }
        return releaseDir.resolve(manifestOverride);
    }
}
