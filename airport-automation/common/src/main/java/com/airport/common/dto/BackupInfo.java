package com.airport.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Информация о файле резервной копии на сервере.
 */
public class BackupInfo implements Serializable, Comparable<BackupInfo> {

    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final long sizeBytes;
    private final LocalDateTime createdAt;

    public BackupInfo(String fileName, long sizeBytes, LocalDateTime createdAt) {
        this.fileName = fileName;
        this.sizeBytes = sizeBytes;
        this.createdAt = createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public int compareTo(BackupInfo other) {
        return other.createdAt.compareTo(createdAt);
    }
}
