package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.*;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public String welcome() {
        return "Welcome to Skill Drill!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterDto user) {
            User newUser = new User();
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setFullname(user.getFullname());

        userService.registerUser(newUser);
        return ResponseEntity.ok().body(Map.of("message", "User registered successfully"));
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<Map<String, String>> googleLogin(@RequestBody OAuthRequest request) throws GeneralSecurityException, IOException {
        String appJwt = this.userService.verifyAndProcessGoogleUser(request.getIdToken());

        ResponseCookie cookie = ResponseCookie.from("jwt-token", appJwt)
                .httpOnly(true)
//                .secure(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 2)
                .sameSite("Lax")
//                .domain(".skill-drill.dev")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of(
                        "message", "Google login successful"
                ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginCredential user) {
        String response = this.userService.verifyUser(user);

        ResponseCookie cookie = ResponseCookie.from("jwt-token", response)
                .httpOnly(true)
//                .secure(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 2)
                .sameSite("Lax")
//                .sameSite("None")
//                .domain(".skill-drill.dev")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "User logged in successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie expiredCookie = ResponseCookie.from("jwt-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .domain(".skill-drill.dev")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, expiredCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/forget-password")
    public ResponseEntity<?> forgetPassword(@RequestParam("email") String email) {
        userService.generateResetToken(email);
        return ResponseEntity.ok(Map.of("message", "Reset link successfully sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPassword dto) {
        userService.resetPassword(dto);
        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }

    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assert auth != null;
        String current_role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("ANONYMOUS");

        Map<String, Object> response = new HashMap<>();
        response.put("username", auth.getName());
        response.put("AssignedAuthority", current_role);
        response.put("requiresVerification", current_role.equals("ROLE_UNVERIFIED"));
        return  ResponseEntity.ok().body(response);
    }

}
