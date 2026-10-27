package com.careerbridge.controller;

import com.careerbridge.dto.response.AdminDashboardResponse;
import com.careerbridge.service.AdminService;
import com.careerbridge.service.JdbcDatabasePersistenceService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final JdbcDatabasePersistenceService jdbcDatabasePersistenceService;

    public AdminController(AdminService adminService, JdbcDatabasePersistenceService jdbcDatabasePersistenceService) {
        this.adminService = adminService;
        this.jdbcDatabasePersistenceService = jdbcDatabasePersistenceService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        return ResponseEntity.ok(adminService.getAdminDashboardStats());
    }

    /**
     * JDBC-based database status inspection
     */
    @GetMapping("/database/status")
    public ResponseEntity<JdbcDatabasePersistenceService.DatabaseStatusDto> getDatabaseStatus() {
        return ResponseEntity.ok(jdbcDatabasePersistenceService.getDatabaseStatus());
    }

    /**
     * Executes JDBC checkpoint sync to immediately write and save all changes to disk
     */
    @PostMapping("/database/checkpoint")
    public ResponseEntity<Map<String, Object>> checkpointDatabase() {
        return ResponseEntity.ok(jdbcDatabasePersistenceService.flushCheckpointToDisk());
    }

    /**
     * Creates and saves a complete SQL script dump via JDBC
     */
    @PostMapping("/database/backup")
    public ResponseEntity<JdbcDatabasePersistenceService.BackupFileDto> createBackup(
            @RequestParam(required = false) String note) {
        return ResponseEntity.ok(jdbcDatabasePersistenceService.saveDatabaseBackup(note));
    }

    /**
     * Lists all saved SQL database backups
     */
    @GetMapping("/database/backups")
    public ResponseEntity<List<JdbcDatabasePersistenceService.BackupFileDto>> listBackups() {
        return ResponseEntity.ok(jdbcDatabasePersistenceService.listBackups());
    }

    /**
     * Downloads a saved SQL database backup
     */
    @GetMapping("/database/backups/{fileName}/download")
    public ResponseEntity<byte[]> downloadBackup(@PathVariable String fileName) {
        try {
            byte[] data = jdbcDatabasePersistenceService.getBackupContent(fileName);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(data);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Direct JDBC export of any table's records
     */
    @GetMapping("/database/export/{tableName}")
    public ResponseEntity<List<Map<String, Object>>> exportTable(@PathVariable String tableName) {
        return ResponseEntity.ok(jdbcDatabasePersistenceService.exportTableData(tableName));
    }
}
