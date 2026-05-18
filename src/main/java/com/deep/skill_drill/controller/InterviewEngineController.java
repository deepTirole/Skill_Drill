package com.deep.skill_drill.controller;

import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.services.InterviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interview")
@CrossOrigin("*")
public class InterviewEngineController {

    private final InterviewService interviewService;

    public InterviewEngineController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/start")
    public ResponseEntity<List<QaLog>> startInterviewEngine(
        @RequestParam("uId") Long userId,
        @RequestParam("job-role") String jobRole
    ) {

        try {
            return ResponseEntity.ok(
                    interviewService.fetchAndSaveInterview(jobRole, userId)
            );
        }
        catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
