package io.github.mikhirurg.jbackup.exceptions;

public class NoFullRestoreException extends JBackupException {
    public NoFullRestoreException() {
        super("The last restore point in the chain has no base!");
    }
}
