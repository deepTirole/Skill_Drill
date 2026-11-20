package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.OtpPayload;
import com.deep.skill_drill.exceptions.BadRequestException;
import com.deep.skill_drill.exceptions.GoneException;
import com.deep.skill_drill.exceptions.TooManyRequestsException;
import jakarta.validation.Valid;
import com.deep.skill_drill.dto.OtpResponse;
import com.deep.skill_drill.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final UserService userService;

    public MailController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<OtpResponse> resendOtp(@RequestBody String email) throws UnsupportedEncodingException {
        userService.resendOtp(email);
        return ResponseEntity.ok(new OtpResponse("A 6-DIGIT OTP SENT TO YOUR EMAIL", true));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody OtpPayload otpPayload) {
        String res = userService.verifyAndEnableUser(otpPayload.getEmail(), otpPayload.getOtp());

        return switch (res) {
            case "SUCCESS" -> ResponseEntity.ok(Map.of("message", "Registration successful"));
            case "INVALID_OTP" -> throw new BadRequestException("INVALID_OTP", "Invalid OTP");
            case "TOO_MANY_ATTEMPTS" -> throw new TooManyRequestsException("OTP_ATTEMPTS_EXCEEDED", "Too many OTP attempts");
            case "SESSION_EXPIRED" -> throw new GoneException("OTP_SESSION_EXPIRED", "Session expired, please register again");
            case "OTP_EXPIRED" -> throw new BadRequestException("OTP_EXPIRED", "OTP expired, a new OTP was sent to your email");
            default -> throw new com.deep.skill_drill.exceptions.ExternalServiceException("OTP_PROCESSING_FAILED", "OTP verification could not be completed");
        };
    }
}