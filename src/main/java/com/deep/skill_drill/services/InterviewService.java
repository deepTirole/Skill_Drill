package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.AnswerSubmitDTO;
import com.deep.skill_drill.dto.AiCritiqueDTO;
import com.deep.skill_drill.dto.InterviewDTO;
import com.deep.skill_drill.dto.QaLogDTO;
import com.deep.skill_drill.dto.SessionResult;
import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.InterviewRepo;
import com.deep.skill_drill.repositories.QaLogsRepo;
import com.deep.skill_drill.repositories.UserRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {

    private final InterviewRepo interviewRepo;
    private final QaLogsRepo qaLogsRepo;
    private final QaLogService qaLogService;
    private final AiService aiService;
    private final UserRepo userRepo;
    private final RatingService ratingService;
    private final ObjectMapper objectMapper;

    public InterviewService(InterviewRepo interviewRepo, QaLogsRepo qaLogsRepo, AiService aiService,
                            UserRepo userRepo, RatingService ratingService,
                            QaLogService qaLogService, ObjectMapper objectMapper) {
        this.interviewRepo = interviewRepo;
        this.qaLogsRepo = qaLogsRepo;
        this.aiService = aiService;
        this.userRepo = userRepo;
        this.ratingService = ratingService;
        this.qaLogService = qaLogService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public InterviewDTO fetchAndSaveInterview(String jobRole, Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with ID: " + userId));

        Interview interview = new Interview();
        interview.setStatus("Pending");
        interview.setJobRole(jobRole);
        interview.setUser(user);

        int rating = user.getRating();
        String difficulty = ratingService.getDifficultyInstructions(rating);

        Interview savedInterview = interviewRepo.save(interview);
        List<String> aiGeneratedQues = aiService.fetchAiQuestions(jobRole, difficulty, user);

        qaLogService.saveQuestions(savedInterview, aiGeneratedQues);

        return sendInterview(savedInterview, userId);
    }

    private InterviewDTO sendInterview(Interview savedInterview, Long userId) {
        InterviewDTO interviewDTO = new InterviewDTO();
        interviewDTO.setId(savedInterview.getId());
        interviewDTO.setRole(savedInterview.getJobRole());
        interviewDTO.setCreatedAt(savedInterview.getStartTime());
        interviewDTO.setUserId(userId);
        interviewDTO.setStatus(savedInterview.getStatus());
        return interviewDTO;
    }

    private QaLogDTO toQaLogDTO(QaLog log) {
        QaLogDTO dto = new QaLogDTO();
        dto.setId(log.getId());
        dto.setQuestion(log.getQuestion());
        dto.setUserAnswer(log.getUserAnswer());

        try {
            AiCritiqueDTO critique = objectMapper.readValue(log.getAiFeedback(), AiCritiqueDTO.class);
            dto.setAiCritique(critique);
        } catch (Exception e) {
            AiCritiqueDTO fallback = new AiCritiqueDTO();
            fallback.setScore(0.0);
            fallback.setStrengths(List.of());
            fallback.setFeedbackLines(List.of());
            fallback.setMissingTopics(List.of());
            dto.setAiCritique(fallback);
        }
        return dto;
    }

    @Transactional
    public @Nullable SessionResult calculateFinalScore(Long sessionId, AnswerSubmitDTO finalAnswer) {
        QaLog qaLogToSave = qaLogsRepo.findById(finalAnswer.getQaLogId())
                .orElseThrow(() -> new EntityNotFoundException("QaLog not found for ID: " + finalAnswer.getQaLogId()));

        qaLogToSave.setUserAnswer(finalAnswer.getUserResponse());

        String res = aiService.generateResponse(qaLogToSave);
        qaLogToSave.setAiFeedback(res);
        qaLogsRepo.save(qaLogToSave);

        Interview interview = interviewRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Interview not found for ID: " + sessionId));

        List<QaLog> allInterviewQuestions = qaLogsRepo.findAllByInterviewId(sessionId);

        List<QaLogDTO> dtos = allInterviewQuestions.stream()
                .map(this::toQaLogDTO)
                .toList();

        double finalScore = dtos.stream()
                .mapToDouble(log -> log.getAiCritique().getScore())
                .average()
                .orElse(0.0);

        User user = interview.getUser();
        int updatedRating = ratingService.dynamicRatingUpdate(user.getRating(), finalScore);
        int eloChange = updatedRating - user.getRating();

        user.setRating(updatedRating);
        interview.setRatingAfter(updatedRating);
        interview.setEloChange(eloChange);
        interview.setCompletionTime(LocalDateTime.now());
        interview.setStatus("Completed");
        interview.setFinalScore(String.valueOf(finalScore));

        userRepo.save(user);
        interviewRepo.save(interview);

        SessionResult sessionResult = new SessionResult();
        sessionResult.setInterview(interview);
        sessionResult.setFinalScore(finalScore);
        sessionResult.setEloChange(eloChange);
        sessionResult.setNewElo(updatedRating);
        sessionResult.setQaLogs(dtos);
        sessionResult.setRole(interview.getJobRole());

        return sessionResult;
    }

    public @Nullable SessionResult getHistoryById(Long sessionId) {
        Interview interview = interviewRepo.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Session not found for ID: " + sessionId));

        List<QaLog> logs = qaLogsRepo.findAllByInterviewId(sessionId);

        SessionResult sessionResult = new SessionResult();
        sessionResult.setInterview(interview);
        sessionResult.setNewElo(interview.getRatingAfter());
        sessionResult.setFinalScore(Double.parseDouble(interview.getFinalScore()));
        sessionResult.setQaLogs(logs.stream().map(this::toQaLogDTO).toList());
        sessionResult.setEloChange(interview.getEloChange());
        sessionResult.setRole(interview.getJobRole());

        return sessionResult;
    }

    public List<Interview> getUserHistory(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        List<Interview> records = interviewRepo.findByUserId(user.getId());
        if (records.isEmpty()) {
            throw new EntityNotFoundException("No Interview History found for user: " + username);
        }
        return records;
    }
}