package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.AnswerSubmitDTO;
import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.repositories.InterviewRepo;
import com.deep.skill_drill.repositories.QaLogsRepo;
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

    @Autowired
    private QaLogsRepo qaLogsRepo;
    @Autowired
    @Lazy
    private QaLogService self;
    @Autowired
    private AiService aiService;
    @Autowired
    private InterviewRepo interviewRepo;

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
                .orElseThrow(() -> new RuntimeException("QaLog not found"));

        qaLog.setUserAnswer(answerSubmitDTO.getUserResponse());
        QaLog qaLog1 = qaLogsRepo.saveAndFlush(qaLog);
        self.storeFeedback(qaLog);

        return "Answer is submitted and evaluating in background";
    }

    @Async
    public void storeFeedback(QaLog qaLog) {
        String aiResponse = aiService.generateResponse(qaLog);
        qaLog.setAiFeedback(aiResponse);
        qaLogsRepo.save(qaLog);
    }

    public List<QaLog> getQuestions(Long interviewId) {
        return qaLogsRepo.findAllByInterviewId(interviewId);
    }

}
