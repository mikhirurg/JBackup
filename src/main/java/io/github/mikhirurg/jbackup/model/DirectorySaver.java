package io.github.mikhirurg.jbackup.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class DirectorySaver extends FileSaver {

    private final Path path;

    public DirectorySaver(Path path) {
        this.path = path;
    }

    @Override
    public void save(Backup backup) throws IOException {
        if (Files.exists(path)) {
            try (Stream<Path> walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                        .forEach(e -> {
                            try {
                                Files.delete(e);
                            } catch (IOException ignored) {

                            }
                        });
            }
        }
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
