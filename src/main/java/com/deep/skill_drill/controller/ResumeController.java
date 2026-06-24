package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.ResumeMetaDto;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
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
import java.util.Set;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
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
                                       Authentication authentication) throws IOException
    {
        System.out.println("upload hit");
        try {
            String username = authentication.getName();
            User user = userRepo.findByUsername(username);
            if(user==null)
                throw new UsernameNotFoundException("Username not found");

            return ResponseEntity.ok(resumeService.saveResume(file, user));
        } catch (IOException | TikaException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/get-resume")
    public ResponseEntity<?> getResume(
            Authentication authentication
    ) {
        String username = authentication.getName();
        if(username==null)
            throw new UsernameNotFoundException("Username not found");
        ResumeMetaDto resume = resumeService.getResume(username);
        if(resume==null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);

        return ResponseEntity.ok(resume);
    }

    @GetMapping("/get_user_skills")
    public ResponseEntity<Set<Skill>> getUserSkills(Authentication authentication) {
        String username = authentication.getName();
        if(username==null)
            throw new UsernameNotFoundException("Username not found");

        return ResponseEntity.ok(resumeService.getUserSkills(username));
    }

}
