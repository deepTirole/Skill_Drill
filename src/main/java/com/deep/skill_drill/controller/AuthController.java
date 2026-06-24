package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.LoginCredential;
import com.deep.skill_drill.dto.AuthResponse;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService userService;

    class RegisterDto {
        private String username;
        private String password;
        private String fullname;

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getFullname() {
            return fullname;
        }
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to Skill Drill!";
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterDto user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(user.getPassword());
        newUser.setFullname(user.getFullname());

        userService.registerUser(newUser);
        return ResponseEntity.ok().body(Map.of("message","User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginCredential user) {
        String response = this.userService.verifyUser(user);

        if("Invalid username or password".equals(response))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    Map.of("error", "Invalid credentials.")
            );

        User user1 = userService.getUser(user.getUsername());
        AuthResponse authResponse = new AuthResponse(response, user1);

        return ResponseEntity.ok().body(authResponse);
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
