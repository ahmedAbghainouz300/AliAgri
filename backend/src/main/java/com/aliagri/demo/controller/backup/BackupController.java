package com.aliagri.demo.controller.backup;

import com.aliagri.demo.dto.backup.BackupResponse;
import com.aliagri.demo.service.backup.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final BackupService backupService;

    @GetMapping("/list")
    public List<BackupResponse> listBackups() throws Exception {
        return backupService.listBackups().stream()
                .map(f -> new BackupResponse(f.getName(), f.length(), f.lastModified()))
                .toList();
    }

    @PostMapping("/run")
    public ResponseEntity<?> runBackup() {
        try {
            File file = backupService.performBackup();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "file", file.getName(),
                    "size", file.length()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadBackup(@PathVariable String filename) {
        File file = backupService.getBackupFile(filename);
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}