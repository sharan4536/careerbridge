package com.careerbridge.service;

import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Low-level database management service utilizing JDBC for metadata diagnostics,
 * synchronous checkpointing, and raw SQL backups.
 */
@Service
public class JdbcDatabasePersistenceService {

    private static final Logger log = LoggerFactory.getLogger(JdbcDatabasePersistenceService.class);
    private static final String BACKUP_DIR = "./backups";

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public JdbcDatabasePersistenceService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    @Data
    @Builder
    public static class BackupFileDto {
        private String fileName;
        private String filePath;
        private long sizeBytes;
        private String formattedSize;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class DatabaseStatusDto {
        private String databaseProductName;
        private String databaseProductVersion;
        private String jdbcDriverName;
        private String connectionUrl;
        private int tableCount;
        private List<String> tableNames;
        private Map<String, Long> tableRowCounts;
        private int totalBackups;
        private String lastCheckpointTime;
    }

    private void ensureBackupDirectory() {
        File dir = new File(BACKUP_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public Map<String, Object> flushCheckpointToDisk() {
        log.info("Executing JDBC CHECKPOINT SYNC...");
        jdbcTemplate.execute("CHECKPOINT SYNC");
        Map<String, Object> res = new HashMap<>();
        res.put("status", "SUCCESS");
        res.put("message", "Database successfully synchronized to disk via JDBC CHECKPOINT SYNC");
        res.put("timestamp", LocalDateTime.now());
        return res;
    }

    public BackupFileDto saveDatabaseBackup(String optionalNote) {
        ensureBackupDirectory();
        flushCheckpointToDisk();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String cleanNote = (optionalNote != null && !optionalNote.trim().isEmpty())
                ? "_" + optionalNote.replaceAll("[^a-zA-Z0-9_-]", "")
                : "";
        String fileName = "careerbridge_backup_" + timestamp + cleanNote + ".sql";
        Path backupPath = Paths.get(BACKUP_DIR, fileName).toAbsolutePath();

        log.info("Generating JDBC SCRIPT backup to: {}", backupPath);
        jdbcTemplate.execute("SCRIPT TO '" + backupPath.toString().replace("\\", "/") + "'");

        File savedFile = backupPath.toFile();
        long size = savedFile.exists() ? savedFile.length() : 0;

        return BackupFileDto.builder()
                .fileName(fileName)
                .filePath(backupPath.toString())
                .sizeBytes(size)
                .formattedSize(formatSize(size))
                .createdAt(LocalDateTime.now())
                .build();
    }

    public List<BackupFileDto> listBackups() {
        ensureBackupDirectory();
        File dir = new File(BACKUP_DIR);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".sql") || name.endsWith(".zip"));

        List<BackupFileDto> list = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
            for (File file : files) {
                list.add(BackupFileDto.builder()
                        .fileName(file.getName())
                        .filePath(file.getAbsolutePath())
                        .sizeBytes(file.length())
                        .formattedSize(formatSize(file.length()))
                        .createdAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(file.lastModified()), ZoneId.systemDefault()))
                        .build());
            }
        }
        return list;
    }

    public byte[] getBackupContent(String fileName) throws Exception {
        Path filePath = Paths.get(BACKUP_DIR, fileName).normalize();
        if (!filePath.startsWith(Paths.get(BACKUP_DIR).normalize())) {
            throw new SecurityException("Unauthorized file path access");
        }
        return Files.readAllBytes(filePath);
    }

    public List<Map<String, Object>> exportTableData(String tableName) {
        String sanitized = tableName.replaceAll("[^a-zA-Z0-9_]", "").toUpperCase();
        return jdbcTemplate.queryForList("SELECT * FROM " + sanitized);
    }

    public DatabaseStatusDto getDatabaseStatus() {
        try (Connection conn = dataSource.getConnection()) {
            DatabaseMetaData meta = conn.getMetaData();
            String prodName = meta.getDatabaseProductName();
            String prodVer = meta.getDatabaseProductVersion();
            String driver = meta.getDriverName() + " " + meta.getDriverVersion();
            String url = meta.getURL();

            List<String> tables = new ArrayList<>();
            Map<String, Long> rowCounts = new HashMap<>();

            try (ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tbl = rs.getString("TABLE_NAME");
                    tables.add(tbl);
                    try {
                        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tbl, Long.class);
                        rowCounts.put(tbl, count != null ? count : 0L);
                    } catch (Exception ignored) {
                    }
                }
            }

            return DatabaseStatusDto.builder()
                    .databaseProductName(prodName)
                    .databaseProductVersion(prodVer)
                    .jdbcDriverName(driver)
                    .connectionUrl(url)
                    .tableCount(tables.size())
                    .tableNames(tables)
                    .tableRowCounts(rowCounts)
                    .totalBackups(listBackups().size())
                    .lastCheckpointTime(LocalDateTime.now().toString())
                    .build();
        } catch (Exception e) {
            log.error("Failed to query JDBC metadata", e);
            throw new RuntimeException("Could not inspect database metadata via JDBC", e);
        }
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
