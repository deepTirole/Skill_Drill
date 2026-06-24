package com.deep.skill_drill.dto;

import java.util.List;

public class AiCritiqueDTO {
    private double score;
    private int clarity;
    private int depth;
    private int accuracy;
    private List<String> strengths;
    private List<String> feedbackLines;
    private List<String> missingTopics;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public int getClarity() {
        return clarity;
    }

    public void setClarity(int clarity) {
        this.clarity = clarity;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(int accuracy) {
        this.accuracy = accuracy;
    }

    public List<String> getStrengths() {
        return strengths;
    }

    public void setStrengths(List<String> strengths) {
        this.strengths = strengths;
    }

    public List<String> getFeedbackLines() {
        return feedbackLines;
    }

    public void setFeedbackLines(List<String> feedbackLines) {
        this.feedbackLines = feedbackLines;
    }

    public List<String> getMissingTopics() {
        return missingTopics;
    }

    public void setMissingTopics(List<String> missingTopics) {
        this.missingTopics = missingTopics;
    }
}
