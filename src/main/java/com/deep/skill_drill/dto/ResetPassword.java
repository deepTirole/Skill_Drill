package com.deep.skill_drill.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResetPassword {

    @NotBlank(message = "token is required")

    private String token;
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must contain 8 to 72 characters")
    private String password;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
