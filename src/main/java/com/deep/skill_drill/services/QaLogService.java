package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.AnswerSubmitDTO;
import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.repositories.QaLogsRepo;
import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class QaLogService {

    private final QaLogsRepo qaLogsRepo;
    private final AiService aiService;

    public QaLogService(QaLogsRepo qaLogsRepo, AiService aiService) {
        this.qaLogsRepo = qaLogsRepo;
        this.aiService = aiService;
    }

    @Autowired
    @Lazy
    private QaLogService self;

    @Transactional
    public void saveQuestions(Interview interview, List<String> aiGeneratedQues) {
        List<QaLog> qaLogsToSave = new ArrayList<>();

        for(String aiQuestion : aiGeneratedQues) {
            QaLog qaLog = new QaLog();
            qaLog.setQuestion(aiQuestion);
            qaLog.setInterview(interview);
            qaLogsToSave.add(qaLog);
        }

        qaLogsRepo.saveAll(qaLogsToSave);
    }

    public @Nullable String storeUserResponse(AnswerSubmitDTO answerSubmitDTO) {
        QaLog qaLog = qaLogsRepo.findById(answerSubmitDTO.getQaLogId())
                .orElseThrow(() -> new EntityNotFoundException("QaLog not found for ID: " + answerSubmitDTO.getQaLogId()));

        qaLog.setUserAnswer(answerSubmitDTO.getUserResponse());

        qaLogsRepo.saveAndFlush(qaLog);

        self.storeFeedback(qaLog);

        return "Answer submitted and evaluating in background.";
    }

    @Async
    public void storeFeedback(QaLog qaLog) {
        try {
            String aiResponse = aiService.generateResponse(qaLog);
            qaLog.setAiFeedback(aiResponse);
        } catch (Exception e) {
            qaLog.setAiFeedback("{\"score\": 0.0, \"critique\": \"AI Evaluation Failed due to network timeout or rate limit.\"}");
        } finally {
            qaLogsRepo.save(qaLog);
        }
    }

    public List<QaLog> getQuestions(Long interviewId) {
        return qaLogsRepo.findAllByInterviewId(interviewId);
    }
}