package com.aliagri.demo.service.backup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BackupService {

    @Value("${spring.datasource.username:aliagri}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @Value("${backup.directory:/backups}")
    private String backupDir;

    @Value("${backup.max-count:30}")
    private int maxBackupCount;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    @Scheduled(cron = "0 0 2 * * *", zone = "Africa/Casablanca")
    public void scheduledBackup() {
        log.info("Starting scheduled backup...");
        try {
            File backup = performBackup();
            log.info("Scheduled backup completed: {}", backup.getAbsolutePath());
        } catch (Exception e) {
            log.error("Scheduled backup FAILED", e);
        }
    }

    public File performBackup() throws IOException, InterruptedException {
        Path dir = Paths.get(backupDir);
        Files.createDirectories(dir);

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String filename = "aliagri_backup_" + timestamp + ".sql.gz";
        Path tempFile = dir.resolve(filename + ".tmp");
        Path finalFile = dir.resolve(filename);

        String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (dbUrl == null)
            throw new IllegalStateException("SPRING_DATASOURCE_URL is not set");
        String dbName = extractDbName(dbUrl);

        ProcessBuilder pb = new ProcessBuilder(
                "bash", "-c",
                String.format("pg_dump -U %s -h 127.0.0.1 -p 5432 -d %s --clean --if-exists | gzip > %s",
                        dbUser, dbName, tempFile.toAbsolutePath()));
        pb.environment().put("PGPASSWORD", dbPassword);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        try (var reader = process.inputReader()) {
            String line;
            while ((line = reader.readLine()) != null)
                output.append(line).append("\n");
        }

        boolean finished = process.waitFor(5, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Backup timed out");
        }
        if (process.exitValue() != 0) {
            throw new RuntimeException("pg_dump failed (exit " + process.exitValue() + "):\n" + output);
        }
        if (Files.size(tempFile) == 0) {
            Files.delete(tempFile);
            throw new RuntimeException("Backup file is empty");
        }

        Files.move(tempFile, finalFile);
        cleanupOldBackups();
        return finalFile.toFile();
    }

    public List<File> listBackups() throws IOException {
        Path dir = Paths.get(backupDir);
        if (!Files.exists(dir))
            return List.of();
        try (var stream = Files.list(dir)) {
            return stream
                    .filter(p -> p.getFileName().toString().endsWith(".sql.gz"))
                    .map(Path::toFile)
                    .sorted(Comparator.comparing(File::getName).reversed())
                    .collect(Collectors.toList());
        }
    }

    public File getBackupFile(String filename) {
        Path path = Paths.get(backupDir).resolve(filename).normalize();
        if (!path.startsWith(backupDir))
            throw new SecurityException("Invalid filename");
        File file = path.toFile();
        if (!file.exists())
            throw new RuntimeException("Backup not found");
        return file;
    }

    private void cleanupOldBackups() throws IOException {
        Path dir = Paths.get(backupDir);
        try (var stream = Files.list(dir)) {
            List<Path> backups = stream
                    .filter(p -> p.getFileName().toString().startsWith("aliagri_backup_"))
                    .filter(p -> p.getFileName().toString().endsWith(".sql.gz"))
                    .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                    .collect(Collectors.toList());

            if (backups.size() > maxBackupCount) {
                int toDelete = backups.size() - maxBackupCount;
                for (int i = 0; i < toDelete; i++) {
                    Files.delete(backups.get(i));
                    log.info("Deleted old backup: {}", backups.get(i).getFileName());
                }
            }
        }
    }

    private String extractDbName(String jdbcUrl) {
        String withoutPrefix = jdbcUrl.replace("jdbc:postgresql://", "");
        String dbPart = withoutPrefix.substring(withoutPrefix.lastIndexOf("/") + 1);
        int queryIdx = dbPart.indexOf('?');
        if (queryIdx > 0)
            dbPart = dbPart.substring(0, queryIdx);
        return dbPart;
    }
}