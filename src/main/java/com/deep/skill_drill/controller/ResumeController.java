package com.deep.skill_drill.controller;

import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.AiService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin("*")
@RequestMapping("/resume")
public class ResumeController {

    @Autowired
    private AiService aiService;

    @PostMapping("/upload")
    public ResponseEntity<User> upload(@RequestParam("file") MultipartFile file,
                                       @RequestParam String username)
    {
        try {
            return ResponseEntity.ok(aiService.saveResume(file, username));
        } catch (IOException | TikaException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
