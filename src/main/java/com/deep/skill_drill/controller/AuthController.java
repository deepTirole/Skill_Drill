package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.LoginCredential;
import com.deep.skill_drill.dto.RegisterDto;
import com.deep.skill_drill.dto.ResetPassword;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to Skill Drill!";
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> registerUser(@RequestBody RegisterDto user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword());
        newUser.setFullname(user.getFullname());

        userService.registerUser(newUser);
        return ResponseEntity.ok(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> loginUser(@RequestBody LoginCredential user) {
        String response = this.userService.verifyUser(user);

        if ("Invalid username or password".equals(response)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid credentials."));
        }

        ResponseCookie cookie = ResponseCookie.from("jwt-token", response)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 2)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "User logged in successfully"));
    }

    @GetMapping("/forget-password")
    public ResponseEntity<Map<String, String>> forgetPassword(@RequestParam("email") String email) {
        userService.generateResetToken(email);
        return ResponseEntity.ok(Map.of("message", "Reset link successfully sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPassword dto) {
        userService.resetPassword(dto);
        return ResponseEntity.ok(Map.of("SUCCESS", "Password reset successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(Authentication auth) {
        String currentRole = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ANONYMOUS");

        return ResponseEntity.ok(Map.of(
                "username", auth.getName(),
                "AssignedAuthority", currentRole,
                "requiresVerification", currentRole.equals("ROLE_UNVERIFIED")
        ));
    }
}