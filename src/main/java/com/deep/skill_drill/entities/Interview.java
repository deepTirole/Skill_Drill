package com.deep.skill_drill.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "interview_session")
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String jobRole;
    private String status;
    private String finalScore;
    private Integer ratingAfter;
    private Integer eloChange;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime startTime;

    @Column(updatable = false)
    private LocalDateTime completionTime;

    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "interview", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<QaLog> logs = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobRole() {
        return jobRole;
    }

    public void setJobRole(String jobRole) {
        this.jobRole = jobRole;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(String finalScore) {
        this.finalScore = finalScore;
    }

    public Integer getRatingAfter() {
        return ratingAfter;
    }

    public void setRatingAfter(Integer ratingAfter) {
        this.ratingAfter = ratingAfter;
    }

    public Integer getEloChange() {
        return eloChange;
    }

    public void setEloChange(Integer eloChange) {
        this.eloChange = eloChange;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(LocalDateTime completionTime) {
        this.completionTime = completionTime;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<QaLog> getLogs() {
        return logs;
    }

    public void setLogs(List<QaLog> logs) {
        this.logs = logs;
    }
}
