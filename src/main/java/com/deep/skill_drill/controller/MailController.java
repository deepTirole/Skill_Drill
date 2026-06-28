package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.AuthResponse;
import com.deep.skill_drill.dto.OtpPayload;
import com.deep.skill_drill.dto.OtpResponse;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.JwtService;
import com.deep.skill_drill.services.MailService;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;

@RestController
@RequestMapping("/mail")
public class MailController {

    private final UserService userService;
    private final MailService mailService;

    public MailController(UserService userService, MailService mailService) {
        this.userService = userService;
        this.mailService = mailService;
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody String email) {
        try {
            userService.resendOtp(email);
            return ResponseEntity.ok(new OtpResponse(
                    "A 6-DIGIT OTP SENT TO YOUR EMAIL", true));
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new  OtpResponse(
                    "ERROR OCCURRED IN SEND EMAIL", false
            ));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpPayload otpPayload) {
        String res = userService.verifyAndEnableUser(otpPayload.getEmail(), otpPayload.getOtp());

        return switch (res) {
            case "SUCCESS" -> {
                yield ResponseEntity.ok(Map.of("message", "Registration successful"));
            }
            case "INVALID_OTP"      -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("message", "Invalid OTP")
            );
            case "TOO_MANY_ATTEMPTS" -> ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Map.of("message", "Too many attempts")
            );
            case "SESSION_EXPIRED"  -> ResponseEntity.status(HttpStatus.GONE).body(
                    Map.of("message", "Session expired, please register again")
            );
            case "OTP_EXPIRED"      -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("message", "Otp expired, new otp sent to your email")
            );
            default -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", "Something went wrong")
            );
        };
    }


}
