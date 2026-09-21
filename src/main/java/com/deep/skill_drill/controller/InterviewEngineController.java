package com.deep.skill_drill.controller;

import com.deep.skill_drill.dto.AnswerSubmitDTO;
import com.deep.skill_drill.dto.InterviewDTO;
import com.deep.skill_drill.dto.SessionResult;
import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.services.InterviewService;
import com.deep.skill_drill.services.QaLogService;
import com.deep.skill_drill.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/interview")
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
    public ResponseEntity<InterviewDTO> startInterviewEngine(
            Authentication authentication,
            @RequestParam("job-role") String jobRole
    ) {
        User user = userService.getUser(authentication.getName());
        return ResponseEntity.ok(
                interviewService.fetchAndSaveInterview(jobRole, user.getId())
        );
    }

    @GetMapping("/{interviewId}/questions")
    public ResponseEntity<List<QaLog>> getInterviewQuestions(@PathVariable Long interviewId) {
        return ResponseEntity.ok(
                qaLogService.getQuestions(interviewId)
        );
    }

    @PostMapping("/user-response")
    public ResponseEntity<String> userResponse(@RequestBody AnswerSubmitDTO userResponseDTO) {
        return ResponseEntity.ok(qaLogService.storeUserResponse(userResponseDTO));
    }

    @PostMapping("/{sessionId}/finalize")
    public ResponseEntity<SessionResult> getScore(
            @PathVariable Long sessionId,
            @RequestBody AnswerSubmitDTO finalAnswer
    ) {
        return ResponseEntity.ok(
                interviewService.calculateFinalScore(sessionId, finalAnswer)
        );
    }

    @GetMapping("/{sessionId}/result")
    public ResponseEntity<SessionResult> getHistory(@PathVariable Long sessionId) {
        return ResponseEntity.ok(
                interviewService.getHistoryById(sessionId)
        );
    }

    @GetMapping("/my-sessions")
    public ResponseEntity<List<Interview>> getMySessions(Authentication authentication) {
        return ResponseEntity.ok(
                interviewService.getUserHistory(authentication.getName())
        );
    }
}