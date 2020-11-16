package io.github.mikhirurg.jbackup;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public class Test {
    public static void main(String[] args) throws Exception {
        /*BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(List.of(
                Path.of("bak1/fileA.txt"),
                Path.of("bak1/fileB.txt")
        ));
        backup1.addFile(Path.of("bak1/fileC.txt"));
        FullRestorePoint fullRestorePoint = backupManager.createFullRestorePoint(backup1.getId(),
                Path.of("bak1/point1.bak"));
        fullRestorePoint.backup();
        backup1.removeFile(Path.of("bak1/fileA.txt"));
        IncrementalRestorePoint incrementalRestorePoint = backupManager.createIncrementalRestorePoint(backup1.getId(),
                Path.of("bak1/point2.bak"));
        incrementalRestorePoint.backup();

        CleaningAlgorithm cleaningAlgorithm = CleaningAlgorithm.createCleaningAlgorithm()
                .addVolumeLimit(0)
                .addAmountLimit(2)
                .removeIfAll();
        backup1.applyCleaningAlgorithm(cleaningAlgorithm);
        System.out.println(backup1.getRestorePoints().size());*/

        Files.write(Path.of("C:/Users/mikha/IdeaProjects/JBackup/sandbox/case3/100m1.txt"), ByteBuffer.allocate(100 * 1024 * 1024).put((byte) 1).array());
        Files.write(Path.of("C:/Users/mikha/IdeaProjects/JBackup/sandbox/case3/100m2.txt"), ByteBuffer.allocate(100 * 1024 * 1024).put((byte) 2).array());
    }
}
