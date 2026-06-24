package com.deep.skill_drill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SkillDrillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkillDrillApplication.class, args);
    }

}
