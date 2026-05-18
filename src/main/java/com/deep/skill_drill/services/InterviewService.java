package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.InterviewRepo;
import com.deep.skill_drill.repositories.QaLogsRepo;
import com.deep.skill_drill.repositories.UserRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@Service
public class InterviewService {

    private final InterviewRepo interviewRepo;
    private final QaLogsRepo qaLogsRepo;
    private final AiService aiService;
    private final UserRepo userRepo;

    public InterviewService(InterviewRepo interviewRepo,  QaLogsRepo qaLogsRepo
            , AiService aiService, UserRepo userRepo) {
        this.interviewRepo = interviewRepo;
        this.qaLogsRepo = qaLogsRepo;
        this.aiService = aiService;
        this.userRepo = userRepo;
    }

    @Transactional
    public List<QaLog> fetchAndSaveInterview(String jobRole, Long userId) {

        User user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Interview interview = new Interview();
        interview.setStatus("Pending");
        interview.setJobRole(jobRole);
        interview.setUser(user);

        Interview savedInterview = interviewRepo.save(interview);
        List<String> aiGeneratedQues = aiService.fetchAiQuestions(jobRole, user.getUserSkills());

        List<QaLog> qaLogsToSave = new ArrayList<>();

        for(String aiQuestion : aiGeneratedQues) {
            QaLog qaLog = new QaLog();
            qaLog.setQuestion(aiQuestion);
            qaLog.setInterview(savedInterview);

            qaLogsToSave.add(qaLog);
        }

        return qaLogsRepo.saveAll(qaLogsToSave);
    }

}
