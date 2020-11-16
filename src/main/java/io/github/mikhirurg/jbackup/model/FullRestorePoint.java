package io.github.mikhirurg.jbackup.model;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class FullRestorePoint extends RestorePoint {

    private final List<FileInfo> files;
    private final String restoreFile;
    private final LocalDateTime creationDate;

    public FullRestorePoint(Backup backup, String restoreFile) {
        this.restoreFile = restoreFile;
        files = new LinkedList<>();
        backup.getFiles()
                .forEach(e -> {
                    try {
                        files.add(new FileInfo(e));
                    } catch (IOException exception) {
                        exception.printStackTrace();
                    }
                });
        creationDate = FixtureUtils.Time.getCurrentTime();
    }

    @Override
    public void updateFilesList(List<FileInfo> filePaths) {
        filePaths.clear();
        filePaths.addAll(getStoredFiles());
    }

    @Override
    public String generateIndex() {
        return "Backup type: " + getPointType().toString() + "\n" +
                "Creation date: " + creationDate.toString() + "\n" +
                getPaths().stream()
                        .map(e -> e.toAbsolutePath().toString())
                        .collect(Collectors.joining("\n"));
    }

    @Override
    public List<FileInfo> getStoredFiles() {
        return files;
    }

    @Override
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    @Override
    public String getPointName() {
        return restoreFile;
    }

    @Override
    public PointType getPointType() {
        return PointType.FULL;
    }
}
