package com.deep.skill_drill.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterDto {

    @NotBlank(message = "username is required")
    @Email(message = "username must be a valid email address")
    private String username;
    @NotBlank(message = "password is required")
    @Size(min = 8, max = 72, message = "password must contain 8 to 72 characters")
    private String password;
    private String fullname;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }
}
