package io.github.mikhirurg.jbackup.exceptions;

import java.nio.file.Path;

public class FileNotInBackupException extends JBackupException {
    public FileNotInBackupException(Path path) {
        super("File " + path + " is not in backup!");
    }
}
