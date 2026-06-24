package com.deep.skill_drill.dto;

public class AnswerSubmitDTO {

    private Long qaLogId;
    private String userResponse;

    public Long getQaLogId() {
        return qaLogId;
    }

    public void setQaLogId(Long qaLogId) {
        this.qaLogId = qaLogId;
    }

    public String getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(String userResponse) {
        this.userResponse = userResponse;
    }
}
