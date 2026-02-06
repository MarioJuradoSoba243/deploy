package com.fs.pcmens2.deploy.core;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Manifest(PlatformInfo platform, List<ServiceDef> services) {

    public static Manifest load(Path manifestPath) {
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            return mapper.readValue(manifestPath.toFile(), Manifest.class);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer manifest: " + manifestPath, e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PlatformInfo(String name, String version, Integer java) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ServiceDef(
            String name,
            String type,                 // "jar" | "war"
            String artifact,             // relativo al release
            List<String> commonLibs,     // libs adicionales a copiar al deploy
            String targetLibDir,         // para JARs
            String targetDeployPath,     // para WARs
            String systemdUnit,
            Health health,
            String targetFileName        // opcional
    ) {
        public boolean isJar() { return "jar".equalsIgnoreCase(type); }
        public boolean isWar() { return "war".equalsIgnoreCase(type); }

        public String resolvedTargetFileName() {
            if (targetFileName != null && !targetFileName.isBlank()) return targetFileName;
            String src = Path.of(artifact).getFileName().toString();
            return src; // por defecto mantener nombre fuente
        }

        public List<String> commonLibsOrEmpty() {
            return commonLibs == null ? List.of() : commonLibs;
        }

        public Path targetDeployDir() {
            if (isJar()) return Path.of(targetLibDir);
            if (isWar()) {
                Path parent = Path.of(targetDeployPath).getParent();
                if (parent != null) return parent;
            }
            throw new IllegalStateException("No se pudo resolver carpeta deploy para " + name);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Health(String url, Integer timeoutSeconds, Integer retries) {
        public int timeoutSecondsOrDefault() { return Optional.ofNullable(timeoutSeconds).orElse(5); }
        public int retriesOrDefault() { return Optional.ofNullable(retries).orElse(3); }
    }
}
