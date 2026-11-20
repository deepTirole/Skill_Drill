package com.deep.skill_drill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnswerSubmitDTO {

    @NotNull(message = "qaLogId is required")

    private Long qaLogId;
    @NotBlank(message = "userResponse is required")
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
