package com.aliagri.demo.dto.backup;

public record BackupResponse(
        String name,
        long size,
        long modified) {
}