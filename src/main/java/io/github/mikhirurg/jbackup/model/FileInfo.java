package io.github.mikhirurg.jbackup.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileInfo {
    private final Path file;
    private final long volume;

    public FileInfo(Path file) throws IOException {
        this.file = file;
        this.volume = Files.size(file);
    }

    public Path getFile() {
        return file;
    }

    public long getVolume() {
        return volume;
    }
}
