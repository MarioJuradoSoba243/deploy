package com.fs.pcmens2.deploy.core;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;

public class ChecksumVerifier {

    public void verifyRelease(Path releaseDir) {
        Path file = releaseDir.resolve("checksums.sha256");
        if (!Files.exists(file)) throw new IllegalArgumentException("Falta checksums.sha256 en " + releaseDir);
        Map<String, String> entries = parseChecksums(file);
        for (var e : entries.entrySet()) {
            Path target = releaseDir.resolve(e.getKey()).normalize();
            if (!Files.exists(target)) throw new IllegalStateException("Fichero ausente: " + target);
            String sum = sha256(target);
            if (!sum.equalsIgnoreCase(e.getValue())) {
                throw new IllegalStateException("Checksum inválido para " + e.getKey());
            }
        }
    }

    private Map<String,String> parseChecksums(Path file) {
        Map<String,String> map = new LinkedHashMap<>();
        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                // soporta formatos: "<sha>  path" o "<sha> *path"
                String[] parts = line.split("\\s+");
                if (parts.length >= 2) {
                    map.put(parts[parts.length - 1], parts[0]);
                }
            }
        } catch (IOException e) { throw new RuntimeException(e); }
        return map;
    }

    private String sha256(Path file) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file)) {
                is.transferTo(new java.io.OutputStream() {
                    @Override public void write(int b) { md.update((byte)b); }
                    @Override public void write(byte[] b, int off, int len) { md.update(b, off, len); }
                });
            }
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}

