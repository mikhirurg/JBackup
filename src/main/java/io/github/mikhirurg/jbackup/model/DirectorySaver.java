package io.github.mikhirurg.jbackup.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DirectorySaver extends FileSaver {

    private final Path path;

    public DirectorySaver(Path path) {
        this.path = path;
    }

    @Override
    public void save(Backup backup) throws IOException {
        Files.createDirectory(path);
        Path backupIndex = Path.of(path + "/" + backup.getName() + ".index");
        Files.write(backupIndex, backup.generateIndex().getBytes());
        backup.getRestorePoints()
                .forEach(e -> {
                    Path point = Path.of(path + "/" + e.getPointName() + ".point");
                    try {
                        Files.write(point, e.generateIndex().getBytes());
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });
    }
}
