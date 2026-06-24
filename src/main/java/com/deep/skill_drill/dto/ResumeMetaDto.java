package com.deep.skill_drill.dto;

import java.time.LocalDateTime;

public class ResumeMetaDto {
    private String filename;
    private LocalDateTime timestamp;

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
