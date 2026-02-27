package com.fs.pcmens2.deploy.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ManifestResolverTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesDefaultManifestWhenOverrideIsNull() {
        Path releaseDir = tempDir.resolve("release");
        Path resolved = ManifestResolver.resolve(releaseDir, null);

        assertEquals(releaseDir.resolve("manifest.yml"), resolved);
    }

    @Test
    void resolvesRelativeOverrideAgainstReleaseDir() {
        Path releaseDir = tempDir.resolve("release");
        Path resolved = ManifestResolver.resolve(releaseDir, Path.of("custom.yml"));

        assertEquals(releaseDir.resolve("custom.yml"), resolved);
    }

    @Test
    void keepsAbsoluteOverrideAsIs() {
        Path absolute = tempDir.resolve("manifest.yml").toAbsolutePath();
        Path resolved = ManifestResolver.resolve(tempDir.resolve("release"), absolute);

        assertEquals(absolute, resolved);
    }
}
