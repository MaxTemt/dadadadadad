package com.airport.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Текстовый отчёт, сформированный на сервере и сохранённый в файле.
 */
public class ReportResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final String content;
    private final LocalDateTime createdAt;

    public ReportResult(String fileName, String content, LocalDateTime createdAt) {
        this.fileName = fileName;
        this.content = content;
        this.createdAt = createdAt;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
