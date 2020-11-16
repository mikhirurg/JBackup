package io.github.mikhirurg.jbackup.model;

import io.github.mikhirurg.jbackup.exceptions.FileNotInBackupException;
import io.github.mikhirurg.jbackup.exceptions.NoFullRestoreException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Backup {
    private List<RestorePoint> restorePoints;
    private final List<Path> files;
    private final long id;
    private final String name;
    private final FileSaver fileSaver;
    private CleaningAlgorithm cleaningAlgorithm;

    Backup(long id, String name, List<Path> files, FileSaver fileSaver, CleaningAlgorithm cleaningAlgorithm) {
        this.id = id;
        this.files = new LinkedList<>(files);
        this.name = name;
        this.fileSaver = fileSaver;
        this.cleaningAlgorithm = cleaningAlgorithm;
        restorePoints = new LinkedList<>();
    }

    public void save() throws IOException {
        fileSaver.save(this);
    }

    public void setCleaningAlgorithm(CleaningAlgorithm cleaningAlgorithm) {
        this.cleaningAlgorithm = cleaningAlgorithm;
    }

    public long getVolume() {
        return getRestorePoints()
                .stream()
                .map(RestorePoint::getVolume)
                .reduce(Long::sum)
                .orElse(0L);
    }

    private void applyCleaningAlgorithm(CleaningAlgorithm cleaningAlgorithm) {
        long totalVolume = 0;
        int amount = 0;
        int listBorder = restorePoints.size();
        for (int i = restorePoints.size() - 1; i >= 0; i--) {
            if (!cleaningAlgorithm.checkPoint(totalVolume, amount, restorePoints.get(i))) {
                listBorder--;
            } else {
                break;
            }
            totalVolume += restorePoints.get(i).getVolume();
            amount++;
        }

        listBorder = Math.min(listBorder, restorePoints.size() - 1);

        if (restorePoints.get(listBorder).getPointType() == PointType.INCREMENTAL)
            throw new NoFullRestoreException();

        restorePoints = restorePoints.subList(listBorder, restorePoints.size());
    }

    public FullRestorePoint createFullRestorePoint(String name) {
        FullRestorePoint fullRestorePoint = new FullRestorePoint(this, name);
        restorePoints.add(fullRestorePoint);
        applyCleaningAlgorithm(cleaningAlgorithm);
        return fullRestorePoint;
    }

    public IncrementalRestorePoint createIncrementalRestorePoint(String name) {
        if (restorePoints
                .stream()
                .map(e -> e.getPointType() == PointType.FULL)
                .reduce(Boolean::logicalOr).orElseThrow()) {
            IncrementalRestorePoint incrementalRestorePoint = new IncrementalRestorePoint(this, name);
            restorePoints.add(incrementalRestorePoint);
            applyCleaningAlgorithm(cleaningAlgorithm);
            return incrementalRestorePoint;
        } else {
            throw new NoFullRestoreException();
        }
    }

    public String generateIndex() {
        return "Backup name: " + getName() + "\n" +
                "Backup size: " + getVolume() + "\n" +
                "Restore points list: \n" +
                restorePoints.stream()
                        .map(RestorePoint::getPointName)
                        .collect(Collectors.joining("\n"));
    }

    public long getId() {
        return id;
    }

    public List<Path> getFiles() {
        return files;
    }

    public List<RestorePoint> getRestorePoints() {
        return restorePoints;
    }

    public void addFile(Path file) {
        files.add(file);
    }

    public void removeFile(Path file) {
        if (!files.contains(file))
            throw new FileNotInBackupException(file);

        files.remove(file);
    }

    public String getName() {
        return name;
    }
}
