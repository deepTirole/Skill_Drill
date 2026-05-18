package com.deep.skill_drill.controller;

import com.deep.skill_drill.services.MailService;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
public class MailController {
    @Autowired
    private MailService mailService;
    @Autowired
    private UserService userService;

    @GetMapping("/get-otp")
    public ResponseEntity<String> sendOtp(@RequestParam("e") String email) {
        try {
            mailService.sendMail(email);
            return ResponseEntity.ok("OTP sent");
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam("otp") String otp, @RequestParam("e") String email) {
        if(mailService.verifyOtp(otp)) {
            boolean res = userService.verifyAndEnableUser(email);
            if(res)
                return ResponseEntity.ok("OTP verification successful");
            return ResponseEntity.badRequest().body("Username with username not found");
        }
        else {
            return ResponseEntity.badRequest().body("Invalid or expired OTP");
        }
    }
}
