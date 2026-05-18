package com.deep.skill_drill.controller;

import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        User savedUser = this.userService.registerUser(user);
        return ResponseEntity.ok().body(savedUser);
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody User user) {
        String response = this.userService.verifyUser(user);

        if("Invalid username or password".equals(response))
            return ResponseEntity.badRequest().body(response);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
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

    @GetMapping("/token")
    public CsrfToken token(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

}
