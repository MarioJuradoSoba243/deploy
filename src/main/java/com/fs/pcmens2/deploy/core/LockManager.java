package com.fs.pcmens2.deploy.core;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;

public class LockManager implements AutoCloseable {
    private final FileChannel channel;
    private final FileLock lock;

    public LockManager(PlatformPaths paths) {
        try {
            Files.createDirectories(paths.stateDir());
            channel = FileChannel.open(paths.lockFile(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            lock = channel.lock();
        } catch (IOException e) { throw new RuntimeException("No se pudo adquirir lock", e); }
    }

    @Override public void close() {
        try { if (lock != null) lock.release(); } catch (Exception ignored) {}
        try { if (channel != null) channel.close(); } catch (Exception ignored) {}
    }
}

