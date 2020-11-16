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
    public void caseOne() throws Exception {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new ArchiveSaver(Path.of("backup1.zip"))
        );
        RestorePoint restorePoint = backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        restorePoint.generateIndex();

        Assert.assertEquals(2, restorePoint.getStoredFiles().size());
        Set<Path> inner = restorePoint.getStoredFiles().stream().map(FileInfo::getFile).collect(Collectors.toSet());
        Assert.assertTrue(inner.contains(Path.of("fileA.txt")));
        Assert.assertTrue(inner.contains(Path.of("fileB.txt")));

        RestorePoint restorePoint1 = backupManager.createIncrementalRestorePoint(backup1.getId(), "incremental_point1");
        restorePoint1.generateIndex();

        CleaningAlgorithm algorithm = CleaningAlgorithm.createCleaningAlgorithm().addAmountLimit(1);
        try {
            backup1.applyCleaningAlgorithm(algorithm);
            Assert.fail("No exception was thrown");
        } catch (JBackupException e) {

        }

        backup1.save();
    }

    @Test
    public void caseTwo() throws Exception {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1"))
        );
        RestorePoint restorePoint = backupManager.createFullRestorePoint(backup1.getId(), "full_point1");

        Assert.assertEquals(2, restorePoint.getStoredFiles().size());
        Set<Path> inner = restorePoint.getStoredFiles().stream().map(FileInfo::getFile).collect(Collectors.toSet());
        Assert.assertTrue(inner.contains(Path.of("fileA.txt")));
        Assert.assertTrue(inner.contains(Path.of("fileB.txt")));

        backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        CleaningAlgorithm algorithm = CleaningAlgorithm.createCleaningAlgorithm().addAmountLimit(1);
        try {
            backup1.applyCleaningAlgorithm(algorithm);
        } catch (JBackupException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(1, backup1.getRestorePoints().size());

        backup1.save();
    }

    @Test
    public void caseThree() {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("100m1.txt"),
                        Path.of("100m2.txt")
                ),
                new ArchiveSaver(Path.of(""))
        );
        RestorePoint restorePoint = backupManager.createFullRestorePoint(backup1.getId(), "full_point1");

        Assert.assertEquals(2, restorePoint.getStoredFiles().size());
        Set<Path> inner = restorePoint.getStoredFiles().stream().map(FileInfo::getFile).collect(Collectors.toSet());
        Assert.assertTrue(inner.contains(Path.of("100m1.txt")));
        Assert.assertTrue(inner.contains(Path.of("100m1.txt")));

        backupManager.createFullRestorePoint(backup1.getId(), "full_point2");

        Assert.assertEquals(2, backup1.getRestorePoints().size());
        Assert.assertEquals(200 * 1024 * 1024, backup1.getVolume());

        CleaningAlgorithm algorithm = CleaningAlgorithm.createCleaningAlgorithm().addVolumeLimit(150 * 1024 * 1024);
        try {
            backup1.applyCleaningAlgorithm(algorithm);
        } catch (JBackupException e) {
            Assert.fail(e.getMessage());
        }
        Assert.assertEquals(1, backup1.getRestorePoints().size());
    }

    @Test
    public void caseFour() throws IOException {
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
        Assert.assertEquals(1, Files.find(Path.of("backup1"), 1,
                ((path, basicFileAttributes) -> path.equals(Path.of("backup1")))).count());
    }

    @Test
    public void caseFive() throws IOException {
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
        Assert.assertEquals(1, Files.find(Path.of("backup1.zip"), 1,
                ((path, basicFileAttributes) -> path.equals(Path.of("backup1.zip")))).count());
    }

    @Test
    public void caseSix() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1"))
        );
        FixtureUtils.Time.useMockTime(LocalDateTime.of(2020, 11, 12, 0, 0));
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));

        FixtureUtils.Time.useSystemTime();
        backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        CleaningAlgorithm cleaningAlgorithm = CleaningAlgorithm.createCleaningAlgorithm()
                .addMinDate(LocalDateTime.of(2020, 11, 13, 0,0))
                .addAmountLimit(2)
                .removeIfAny();
        backup1.applyCleaningAlgorithm(cleaningAlgorithm);
        Assert.assertEquals(1, backup1.getRestorePoints().size());
        backup1.save();
    }

    @Test
    public void caseSeven() throws IOException {
        BackupManager backupManager = new BackupManager();
        Backup backup1 = backupManager.createBackup(
                "backup1",
                List.of(
                        Path.of("fileA.txt"),
                        Path.of("fileB.txt")
                ),
                new DirectorySaver(Path.of("backup1"))
        );
        FixtureUtils.Time.useMockTime(LocalDateTime.of(2020, 11, 12, 0, 0));
        backupManager.createFullRestorePoint(backup1.getId(), "full_point1");
        backup1.addFile(Path.of("FileC.txt"));

        FixtureUtils.Time.useSystemTime();
        backupManager.createFullRestorePoint(backup1.getId(), "full_point2");
        CleaningAlgorithm cleaningAlgorithm = CleaningAlgorithm.createCleaningAlgorithm()
                .addMinDate(LocalDateTime.of(2020, 11, 13, 0,0))
                .addAmountLimit(1)
                .removeIfAll();

        backup1.applyCleaningAlgorithm(cleaningAlgorithm);
        Assert.assertEquals(1, backup1.getRestorePoints().size());
        backup1.save();
    }
}