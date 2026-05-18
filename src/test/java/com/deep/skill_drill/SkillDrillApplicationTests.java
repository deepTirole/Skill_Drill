package com.deep.skill_drill;

import com.deep.skill_drill.services.AiService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SkillDrillApplicationTests {

    @Autowired
    private AiService aiService;

    @Test
    void contextLoads() {
    }

//    @Test
////    void resume() {
////        String s = resumeService.parseResume();
////        System.out.println(s);
////    }

}
