package com.deep.skill_drill.dto;

import com.deep.skill_drill.entities.Interview;

import java.util.List;

public class SessionResult {
    private Interview interview;
    private List<QaLogDTO> qaLogs;
    private double finalScore;
    private Integer eloChange;
    private Integer newElo;
    private String role;
//    timeSpentSeconds: number;


    public Interview getInterview() {
        return interview;
    }

    public void setInterview(Interview interview) {
        this.interview = interview;
    }

    public List<QaLogDTO> getQaLogs() {
        return qaLogs;
    }

    public void setQaLogs(List<QaLogDTO> qaLogs) {
        this.qaLogs = qaLogs;
    }

    public double getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(double finalScore) {
        this.finalScore = finalScore;
    }

    public Integer getEloChange() {
        return eloChange;
    }

    public void setEloChange(Integer eloChange) {
        this.eloChange = eloChange;
    }

    public Integer getNewElo() {
        return newElo;
    }

    public void setNewElo(Integer newElo) {
        this.newElo = newElo;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
