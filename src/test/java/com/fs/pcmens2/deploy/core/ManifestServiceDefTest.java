package com.fs.pcmens2.deploy.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ManifestServiceDefTest {

    @Test
    void targetDeployDirUsesLibDirForJar() {
        Manifest.ServiceDef service = new Manifest.ServiceDef(
                "svc-a",
                "jar",
                "svc-a.jar",
                List.of("libs/extra.jar"),
                "/opt/pcm/svc-a/lib",
                null,
                "svc-a.service",
                new Manifest.Health("http://localhost/a", 3, 1),
                null
        );

        assertEquals("/opt/pcm/svc-a/lib", service.targetDeployDir().toString());
        assertEquals(List.of("libs/extra.jar"), service.commonLibsOrEmpty());
    }

    @Test
    void targetDeployDirUsesParentForWar() {
        Manifest.ServiceDef service = new Manifest.ServiceDef(
                "svc-web",
                "war",
                "svc-web.war",
                null,
                null,
                "/opt/tomcat/webapps/svc-web.war",
                "svc-web.service",
                new Manifest.Health("http://localhost/web", 3, 1),
                null
        );

        assertEquals("/opt/tomcat/webapps", service.targetDeployDir().toString());
        assertEquals(List.of(), service.commonLibsOrEmpty());
    }

    @Test
    void targetDeployDirFailsWhenWarHasNoParentDir() {
        Manifest.ServiceDef service = new Manifest.ServiceDef(
                "svc-bad",
                "war",
                "svc-bad.war",
                null,
                null,
                "svc-bad.war",
                "svc-bad.service",
                new Manifest.Health("http://localhost/bad", 3, 1),
                null
        );

        assertThrows(IllegalStateException.class, service::targetDeployDir);
    }
}
