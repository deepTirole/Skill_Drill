package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.ResumeMetaDto;
import com.deep.skill_drill.entities.ResumeMetadata;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.exceptions.BadRequestException;
import com.deep.skill_drill.exceptions.ResourceNotFoundException;
import com.deep.skill_drill.repositories.ResumeRepo;
import com.deep.skill_drill.repositories.UserRepo;
import com.deep.skill_drill.services.ResumeService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private ResumeRepo resumeRepo;

    @PostMapping("/upload")
    public ResponseEntity<User> upload(@RequestParam("file") MultipartFile file,
                                       Authentication authentication) {
        String username = authentication.getName();
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "Username not found");
        }
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("RESUME_EMPTY", "A resume file is required");
        }
        return ResponseEntity.ok(resumeService.saveResume(file, user));
    }

    @GetMapping("/get-resume")
    public ResponseEntity<?> getResume(Authentication authentication) {
        return ResponseEntity.ok(resumeService.getResume(authentication.getName()));
    }

    @GetMapping("/get_user_skills")
    public ResponseEntity<Set<Skill>> getUserSkills(Authentication authentication) {
        String username = authentication.getName();
        if(username==null)
            throw new UsernameNotFoundException("Username not found");

        return ResponseEntity.ok(resumeService.getUserSkills(username));
    }

}
