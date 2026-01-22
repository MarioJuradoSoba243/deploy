package com.fs.pcmens2.deploy.core;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public class StateStore {
    private final PlatformPaths paths;
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    public StateStore(PlatformPaths paths) { this.paths = paths; }

    public State load() {
        try {
            paths.ensureBaseDirs();
            Path f = paths.stateFile();
            if (!Files.exists(f)) return new State(null, null, new ArrayList<>(), new ArrayList<>());
            return mapper.readValue(f.toFile(), State.class);
        } catch (IOException e) { throw new RuntimeException(e); }
    }

    public void save(State s) {
        try { mapper.writeValue(paths.stateFile().toFile(), s); }
        catch (IOException e) { throw new RuntimeException(e); }
    }

    public void commitActivation(String newVersion, String previous) {
        State s = load();
        s = s.withActivation(newVersion, previous);
        save(s);
    }

    public void markFailed(String from, String to, String reason) {
        State s = load();
        s.history().add(new History("ACTIVATE", from, to, "FAILED", reason, Instant.now().toString()));
        save(s);
    }

    // --- Modelos ---
    public record State(String currentVersion, String previousVersion,
                        List<String> installedVersions, List<History> history) {
        public State withInstalled(String version) {
            ArrayList<String> list = new ArrayList<>(installedVersions == null ? List.of() : installedVersions);
            if (!list.contains(version)) list.add(version);
            return new State(currentVersion, previousVersion, list, history == null ? new ArrayList<>() : history);
        }
        public State withActivation(String newVersion, String previous) {
            ArrayList<String> list = new ArrayList<>(installedVersions == null ? List.of() : installedVersions);
            if (!list.contains(newVersion)) list.add(newVersion);
            ArrayList<History> hist = new ArrayList<>(history == null ? List.<History>of() : history);
            hist.add(new History("ACTIVATE", previous, newVersion, "SUCCESS", null, Instant.now().toString()));
            return new State(newVersion, previous, list, hist);
        }
    }

    public record History(String action, String from, String to, String result, String reason, String at) {}
}

