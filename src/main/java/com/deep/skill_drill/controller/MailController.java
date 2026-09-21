package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.OtpPayload;
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
    public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody OtpPayload otpPayload) {
        String res = userService.verifyAndEnableUser(otpPayload.getEmail(), otpPayload.getOtp());

        return switch (res) {
            case "SUCCESS" -> ResponseEntity.ok(Map.of("message", "Registration successful"));
            case "INVALID_OTP" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Invalid OTP"));
            case "TOO_MANY_ATTEMPTS" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(Map.of("message", "Too many attempts"));
            case "SESSION_EXPIRED" -> ResponseEntity.status(HttpStatus.GONE).body(Map.of("message", "Session expired, please register again"));
            case "OTP_EXPIRED" -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", "Otp expired, new otp sent to your email"));
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Something went wrong"));
        };
    }
}