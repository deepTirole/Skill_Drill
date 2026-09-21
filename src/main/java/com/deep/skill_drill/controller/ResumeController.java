package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.ResumeMetaDto;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.UserRepo;
import com.deep.skill_drill.services.ResumeService;
import jakarta.persistence.EntityNotFoundException;
import org.apache.tika.exception.TikaException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Set;

@RestController
@RequestMapping("/resume")
public class ResumeController {

    private final ResumeService resumeService;
    private final UserRepo userRepo;

    public ResumeController(ResumeService resumeService, UserRepo userRepo) {
        this.resumeService = resumeService;
        this.userRepo = userRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<User> upload(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException, TikaException {
        User user = userRepo.findByUsername(authentication.getName());
        if (user == null) {
            throw new EntityNotFoundException("User not found.");
        }
        return ResponseEntity.ok(resumeService.saveResume(file, user));
    }

    @GetMapping("/get-resume")
    public ResponseEntity<ResumeMetaDto> getResume(Authentication authentication) {
        return ResponseEntity.ok(resumeService.getResume(authentication.getName()));
    }

    @GetMapping("/get_user_skills")
    public ResponseEntity<Set<Skill>> getUserSkills(Authentication authentication) {
        return ResponseEntity.ok(resumeService.getUserSkills(authentication.getName()));
    }
}