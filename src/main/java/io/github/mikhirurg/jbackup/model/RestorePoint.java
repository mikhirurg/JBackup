package io.github.mikhirurg.jbackup.model;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public abstract class RestorePoint {
    public abstract List<FileInfo> getStoredFiles();

    public List<Path> getPaths() {
        return getStoredFiles()
                .stream()
                .map(FileInfo::getFile)
                .collect(Collectors.toList());
    }

    public long getVolume() {
        return getStoredFiles()
                .stream()
                .map(FileInfo::getVolume)
                .reduce(Long::sum)
                .orElse(0L);
    }

    public abstract LocalDateTime getCreationDate();

    public abstract String getPointName();

    public abstract PointType getPointType();

    public abstract void updateFilesList(List<FileInfo> fileInfos);

    public abstract String generateIndex();
}
