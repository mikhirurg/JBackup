package io.github.mikhirurg.jbackup.model;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ArchiveSaver extends FileSaver {

    private final Path path;

    public ArchiveSaver(Path path) {
        this.path = path;
    }

    @Override
    public void save(Backup backup) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        env.put("encoding", "UTF-8");
        URI uri = URI.create("jar:" + path.toUri());

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(uri, env)) {
            Path backupIndexInZip = zipFileSystem.getPath("/" + backup.getName() + ".index");
            Files.write(backupIndexInZip, backup.generateIndex().getBytes());
            backup.getRestorePoints()
                    .forEach(e -> {
                        try {
                            Path pointInZip = zipFileSystem.getPath("/" + e.getPointName() + ".point");
                            Files.write(pointInZip, e.generateIndex().getBytes());
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    });
        } catch (Exception ignored) {

        }
    }
}
