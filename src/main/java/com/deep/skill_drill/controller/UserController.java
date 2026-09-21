package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.*;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.JwtService;
import com.deep.skill_drill.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/get_user")
    public ResponseEntity<User> getUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }

    @GetMapping("/get_sessions")
    public ResponseEntity<List<RatingPointDto>> getSessions(Authentication authentication) {
        return ResponseEntity.ok(userService.getInterviewRatingHistory(authentication.getName()));
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, String>> update(@RequestBody UpdateDto user, Authentication auth) throws UnsupportedEncodingException {
        userService.updateUser(user, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Field updated successfully."));
    }

    @PostMapping("/update-email")
    public ResponseEntity<Map<String, String>> updateEmail(@RequestBody EmailUpdate dto, Authentication auth) {
        userService.initiateEmailUpdate(dto, auth.getName());
        return ResponseEntity.ok(Map.of("message", "Email updated successfully."));
    }

    @PostMapping("/complete-email-update")
    public ResponseEntity<AuthResponse> updateEmailFinal(@RequestBody OtpPayload dto, Authentication auth) {
        User user = userService.finalizeEmailUpdate(dto, auth.getName());
        String token = jwtService.generateJwtToken(user);
        return ResponseEntity.ok(new AuthResponse(token, user));
    }
}