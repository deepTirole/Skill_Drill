package com.deep.skill_drill;

import com.deep.skill_drill.dto.InterviewDTO;
import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.services.AiService;
import com.deep.skill_drill.services.InterviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkillDrillApplicationTests {

    @Autowired
    private AiService aiService;
    @Autowired
    private InterviewService interviewService;

    @Test
    void contextLoads() {
    }

    @Test
    void resume() {}

//    @Test
////    void resume() {
////        String s = resumeService.parseResume();
////        System.out.println(s);
////    }

    @Test
    public void get() {
        InterviewDTO javaDeveloper = interviewService.fetchAndSaveInterview("Java Developer", 1L);
        System.out.println(javaDeveloper.getCreatedAt());
    }

}
