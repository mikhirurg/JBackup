package io.github.mikhirurg.jbackup.model;

import java.io.IOException;

public abstract class FileSaver {
    public abstract void save(Backup backup) throws IOException;
}
