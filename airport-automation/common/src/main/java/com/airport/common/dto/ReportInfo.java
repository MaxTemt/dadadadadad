package com.airport.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Информация о текстовом отчёте, сохранённом на сервере.
 */
public class ReportInfo implements Serializable, Comparable<ReportInfo> {

    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final long sizeBytes;
    private final LocalDateTime createdAt;

    public ReportInfo(String fileName, long sizeBytes, LocalDateTime createdAt) {
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
    public int compareTo(ReportInfo other) {
        return other.createdAt.compareTo(createdAt);
    }
}
