package com.deep.skill_drill.dto;

import java.time.LocalDateTime;

public class InterviewDTO {
    private Long id;
    private Long userId;
    private String role;
    private String status;
    private LocalDateTime createdAt;
    private Double sessionScore;
    private Integer eloChange;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getSessionScore() {
        return sessionScore;
    }

    public void setSessionScore(Double sessionScore) {
        this.sessionScore = sessionScore;
    }

    public Integer getEloChange() {
        return eloChange;
    }

    public void setEloChange(Integer eloChange) {
        this.eloChange = eloChange;
    }
}
