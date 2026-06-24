package com.deep.skill_drill.dto;

public class QaLogDTO {
    private Long id;
    private String question;
    private String userAnswer;
    private AiCritiqueDTO aiCritique;  // structured, not raw string

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUserAnswer() {
        return userAnswer;
    }

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public AiCritiqueDTO getAiCritique() {
        return aiCritique;
    }

    public void setAiCritique(AiCritiqueDTO aiCritique) {
        this.aiCritique = aiCritique;
    }

    // getters and setters
}
