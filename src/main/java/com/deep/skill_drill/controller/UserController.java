package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.*;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.JwtService;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
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
        String username = authentication.getName();
        return ResponseEntity.ok(userService.getUser(username));
    }

    @GetMapping("/get_sessions")
    public ResponseEntity<List<RatingPointDto>> getSessions(
            Authentication authentication
    ) {
        String username = authentication.getName();

        return ResponseEntity.ok(userService.getInterviewRatingHistory(username));
    }

    @PutMapping("/update")
    public ResponseEntity<?> update(@RequestBody UpdateDto user, Authentication auth) {
        String username = auth.getName();
        try {
            userService.updateUser(user, username);
            return ResponseEntity.status(HttpStatus.OK).body(
                    (Map.of("message", "Field updated successfully."))
            );
        } catch (RuntimeException | UnsupportedEncodingException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    (Map.of("message", "Failed in updating user."))
            );
        }
    }

    @PostMapping("/update-email")
    public ResponseEntity<?> updateEmail(@RequestBody EmailUpdate dto, Authentication auth) {
        String username = auth.getName();
        try {
            userService.initiateEmailUpdate(dto, username);
            return ResponseEntity.status(HttpStatus.OK).body(
                    Map.of("message", "Email updated successfully.")
            );
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", e.getMessage())
            );
        }
    }

    @PostMapping("/complete-email-update")
    public ResponseEntity<?> updateEmailFinal(@RequestBody OtpPayload dto, Authentication auth) {
        String username = auth.getName();
        try {
            User user = userService.finalizeEmailUpdate(dto, username);
            String token = jwtService.generateJwtToken(user);
            return ResponseEntity.status(HttpStatus.OK).body(
                    new AuthResponse(token, user)
            );
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    Map.of("message", e.getMessage())
            );
        }
    }

}
