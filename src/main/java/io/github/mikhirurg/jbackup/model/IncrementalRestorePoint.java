package io.github.mikhirurg.jbackup.model;

import io.github.mikhirurg.jbackup.exceptions.NoFullRestoreException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class IncrementalRestorePoint extends RestorePoint {

    private final List<FileInfo> toAdd;
    private final List<Path> toDelete;
    private final String pointName;
    private final LocalDateTime creationDate;

    public IncrementalRestorePoint(Backup backup, String pointName) {
        toAdd = new LinkedList<>();
        toDelete = new LinkedList<>();
        this.pointName = pointName;
        creationDate = FixtureUtils.Time.getCurrentTime();
        int lastPoint = -1;

        for (int i = backup.getRestorePoints().size() - 1; i >= 0; i--) {
            if (backup.getRestorePoints().get(i).getPointType() == PointType.FULL) {
                lastPoint = i;
            }
        }

        if (lastPoint == -1)
            throw new NoFullRestoreException();

        List<FileInfo> fileInfos = new LinkedList<>();
        for (int i = lastPoint; i < backup.getRestorePoints().size(); i++) {
            fileInfos = backup.getRestorePoints().get(i).updateFilesList(fileInfos);
        }

        List<Path> paths = fileInfos
                .stream()
                .map(FileInfo::getFile)
                .collect(Collectors.toCollection(LinkedList::new));

        backup.getFiles()
                .forEach(e -> {
                    if (!paths.contains(e)) {
                        try {
                            toAdd.add(new FileInfo(e));
                        } catch (IOException exception) {
                            exception.printStackTrace();
                        }
                    }
                });

        paths.forEach(e -> {
            if (!backup.getFiles().contains(e)) {
                toDelete.add(e);
            }
        });
    }

    public List<FileInfo> getNewFiles() {
        return toAdd;
    }

    public List<Path> getFilesToDelete() {
        return toDelete;
    }

    @Override
    public List<FileInfo> updateFilesList(List<FileInfo> fileInfos) {
        fileInfos = fileInfos.stream()
                .filter(e -> !toDelete.contains(e.getFile()))
                .collect(Collectors.toList());
        fileInfos.addAll(getNewFiles());
        return fileInfos;
    }

    @Override
    public String generateIndex() {
        return "Backup type: " + getPointType().toString() + "\n" +
                "Creation date: " + creationDate.toString() + "\n" +
                "Point size: " + getVolume() + "\n" +
                getNewFiles().stream()
                        .map(e -> "+ " + e.getFile().toAbsolutePath().toString())
                        .collect(Collectors.joining("\n")) +
                getFilesToDelete().stream()
                        .map(e -> "- " + e.toAbsolutePath().toString())
                        .collect(Collectors.joining("\n"));
    }

    @Override
    public List<FileInfo> getStoredFiles() {
        return toAdd;
    }

    @Override
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    @Override
    public String getPointName() {
        return pointName;
    }

    @Override
    public PointType getPointType() {
        return PointType.INCREMENTAL;
    }
}
