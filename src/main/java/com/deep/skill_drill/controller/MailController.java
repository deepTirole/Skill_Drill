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
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/mail")
public class MailController {
    @Autowired
    private MailService mailService;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtService jwtService;

    @GetMapping("/verify-otp")
    public ResponseEntity<?> sendOtp(@RequestParam("e") String email) {
        try {
            mailService.sendMail(email);
            return ResponseEntity.ok(new OtpResponse(
                    "A 6-DIGIT OTP SENT TO YOUR EMAIL", true));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(new  OtpResponse(
                    "ERROR OCCURRED IN SEND EMAIL", false
            ));
        }
    }

    @GetMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam("e") String email) {
        try {
            mailService.sendMail(email);
            return ResponseEntity.ok(new OtpResponse(
                    "A 6-DIGIT OTP SENT TO YOUR EMAIL", true));
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(new  OtpResponse(
                    "ERROR OCCURRED IN SEND EMAIL", false
            ));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpPayload otpPayload) {
        if(mailService.verifyOtp(otpPayload.getEmail(), otpPayload.getOtp())) {
            boolean res = userService.verifyAndEnableUser(otpPayload.getEmail());
            if(res) {
                User user    = userService.getUser(otpPayload.getEmail());
                String token = jwtService.generateJwtToken(user);
                AuthResponse authResponse = new AuthResponse(token, user);

                return ResponseEntity.ok(authResponse);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User with username not found");
        }
        else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid OTP");
        }
    }


}
