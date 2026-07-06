package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.RatingPointDto;
import com.deep.skill_drill.dto.RegisterDto;
import com.deep.skill_drill.dto.UpdateDto;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin("*")
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

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
                    (Map.of("message", "Full name updated successfully."))
            );
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    (Map.of("message", "Failed in updating user."))
            );
        }
    }

}
