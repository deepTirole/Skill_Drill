package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.AnswerSubmitDTO;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.InterviewService;
import com.deep.skill_drill.services.QaLogService;
import com.deep.skill_drill.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/interview")
@CrossOrigin("*")
public class InterviewEngineController {

    private final InterviewService interviewService;
    private final UserService userService;
    private final QaLogService qaLogService;

    public InterviewEngineController(InterviewService interviewService, UserService userService,
                                     QaLogService qaLogService) {
        this.interviewService = interviewService;
        this.userService = userService;
        this.qaLogService = qaLogService;
    }

    @PostMapping("/start")
    public ResponseEntity<?> startInterviewEngine(
        Authentication authentication,
        @RequestParam("job-role") String jobRole
    ) {
        String username = authentication.getName();
        User user = userService.getUser(username);
        try {
            return ResponseEntity.ok(
                    interviewService.fetchAndSaveInterview(jobRole, user.getId())
            );
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @GetMapping("/{interviewId}/questions")
    public ResponseEntity<?> getInterviewQuestions(@PathVariable Long interviewId) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                    qaLogService.getQuestions(interviewId)
            );
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/user-response")
    public ResponseEntity<?> userResponse(@RequestBody AnswerSubmitDTO userResponseDTO) {
        return ResponseEntity.ok(qaLogService.storeUserResponse(userResponseDTO));
    }

    @PostMapping("/{sessionId}/finalize")
    public ResponseEntity<?> getScore(@PathVariable Long sessionId,
                                                         @RequestBody AnswerSubmitDTO finalAnswer) {
        try {
            return ResponseEntity.status(HttpStatus.OK).body(
                    (interviewService.calFinalScore(sessionId, finalAnswer))
            );
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.getMessage());
        }
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<?> getHistory(@PathVariable Long sessionId) {
        try{
            return ResponseEntity.status(HttpStatus.OK).body(
                    interviewService.getHistoryById(sessionId)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
