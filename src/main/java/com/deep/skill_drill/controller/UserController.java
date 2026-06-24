package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.RatingPointDto;
import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.InterviewRepo;
import com.deep.skill_drill.repositories.UserRepo;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

}
