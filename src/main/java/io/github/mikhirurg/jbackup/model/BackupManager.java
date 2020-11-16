package io.github.mikhirurg.jbackup.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BackupManager {

    private long uniqBackupId = 0;

    private final Map<Long, Backup> backups;

    public BackupManager() {
        backups = new HashMap<>();
    }

    public Backup createBackup(String name, List<Path> files, FileSaver fileSaver) {
        Backup backup = new Backup(uniqBackupId, name, files, fileSaver);
        backups.put(uniqBackupId, backup);
        uniqBackupId++;
        return backup;
    }

    public FullRestorePoint createFullRestorePoint(long id, String name) {
        // TODO: Exception for unknown id
        return backups.get(id).createFullRestorePoint(name);
    }

    public IncrementalRestorePoint createIncrementalRestorePoint(long id, String name) {
        return backups.get(id).createIncrementalRestorePoint(name);
    }

    public void saveBackup(long id) throws IOException {
        backups.get(id).save();
    }
}
