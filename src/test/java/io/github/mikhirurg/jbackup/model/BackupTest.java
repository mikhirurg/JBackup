package io.github.mikhirurg.jbackup.model;

import io.github.mikhirurg.jbackup.exceptions.JBackupException;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BackupTest {
    @Test
    public void simpleArchiveBackupAmountCleanTest() throws Exception {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new ArchiveSaver(Path.of("backup1.zip")),
                CleaningAlgorithm.createCleaningAlgorithm()
                        .addAmountLimit(1)
        );
        RestorePoint restorePoint = backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        restorePoint.generateIndex();

        Assert.assertEquals(2, restorePoint.getStoredFiles().size());
        Set<Path> inner = restorePoint.getStoredFiles().stream().map(FileInfo::getFile).collect(Collectors.toSet());
        Assert.assertTrue(inner.contains(Path.of("fileA.txt")));
        Assert.assertTrue(inner.contains(Path.of("fileB.txt")));

        try {
            backupManager.createIncrementalRestorePoint(backup1.getId(), "incremental_point1");
            Assert.fail("No exception was thrown");
        } catch (JBackupException ignored) {

        }

        backup1.save();
    }

    @Test
    public void simpleDirBackupAmountCleanTest() throws Exception {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1")),
                CleaningAlgorithm.createCleaningAlgorithm().addAmountLimit(1)
        );
        RestorePoint restorePoint = backupManager.createFullRestorePoint(backup1.getId(), "full_point1");

        Assert.assertEquals(2, restorePoint.getStoredFiles().size());
        Set<Path> inner = restorePoint.getStoredFiles().stream().map(FileInfo::getFile).collect(Collectors.toSet());
        Assert.assertTrue(inner.contains(Path.of("fileA.txt")));
        Assert.assertTrue(inner.contains(Path.of("fileB.txt")));
        try {
            backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        } catch (JBackupException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(1, backup1.getRestorePoints().size());
        backup1.save();
    }

    @Test
    public void simpleArchiveBackupVolumeCleanTest() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("100m1.txt"),
                        Path.of("100m2.txt")
                ),
                new ArchiveSaver(Path.of("backup1.zip")),
                CleaningAlgorithm.createCleaningAlgorithm().addVolumeLimit(150 * 1024 * 1024)
        );
        RestorePoint restorePoint = backupManager.createFullRestorePoint(backup1.getId(), "full_point1");

        Assert.assertEquals(2, restorePoint.getStoredFiles().size());
        Set<Path> inner = restorePoint.getStoredFiles().stream().map(FileInfo::getFile).collect(Collectors.toSet());
        Assert.assertTrue(inner.contains(Path.of("100m1.txt")));
        Assert.assertTrue(inner.contains(Path.of("100m1.txt")));

        try {
            backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        } catch (JBackupException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(1, backup1.getRestorePoints().size());
        Assert.assertEquals(200 * 1024 * 1024, backup1.getVolume());
        backup1.save();
    }

    @Test
    public void dirBackupSaveTest() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1"))
        );
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));
        backupManager.createIncrementalRestorePoint(backup1.getId(), "incremental_point1");
        backup1.save();
        Assert.assertTrue(Files.exists(Path.of("backup1")));
    }

    @Test
    public void archiveBackupSaveTest() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new ArchiveSaver(Path.of("backup1.zip"))
        );
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));
        backupManager.createIncrementalRestorePoint(backup1.getId(), "incremental_point1");
        backup1.save();
        Assert.assertTrue(Files.exists(Path.of("backup1.zip")));
    }

    @Test
    public void simpleDirBackupSizeTimeCleanTest() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1")),
                CleaningAlgorithm.createCleaningAlgorithm()
                        .addMinDate(LocalDateTime.of(2020, 11, 13, 0, 0))
                        .addAmountLimit(2)
                        .removeIfAny()
        );
        FixtureUtils.Time.useMockTime(LocalDateTime.of(2020, 11, 12, 0, 0));
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));

        FixtureUtils.Time.useSystemTime();
        backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        Assert.assertEquals(1, backup1.getRestorePoints().size());
        backup1.save();
    }

    @Test
    public void simpleDirBackupSizeTimeCleanTest2() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1")),
                CleaningAlgorithm.createCleaningAlgorithm()
                        .addMinDate(LocalDateTime.of(2020, 11, 13, 0, 0))
                        .addAmountLimit(1)
                        .removeIfAll()
        );
        FixtureUtils.Time.useMockTime(LocalDateTime.of(2020, 11, 12, 0, 0));
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));

        FixtureUtils.Time.useSystemTime();
        backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        Assert.assertEquals(1, backup1.getRestorePoints().size());
        backup1.save();
    }

    @Test
    public void caseEight() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new ArchiveSaver(Path.of("backup1.zip"))
        );
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));
        backupManager.createIncrementalRestorePoint(backup1.getId(), "incremental_point1");
        backup1.removeFile(Path.of("FileA.txt"));
        backupManager.createIncrementalRestorePoint(backup1.getId(), "incremental_point2");
        backupManager.saveBackup(backup1.getId());
        Assert.assertTrue(Files.exists(Path.of("backup1.zip")));
    }
}