package com.deep.skill_drill.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class OtpPayload {

    @NotBlank(message = "otp is required")
    private String otp;
    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
