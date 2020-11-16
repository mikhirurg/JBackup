package io.github.mikhirurg.jbackup.exceptions;

public class BackupNotExistsException extends JBackupException {
    public BackupNotExistsException(long id) {
        super("Backup with id: " + id + " does not exists!");
    }
}
