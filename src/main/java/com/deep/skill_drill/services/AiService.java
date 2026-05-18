package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final UserService userService;

    public AiService(
            @Qualifier("googleGenAiChatClient") ChatClient client,
            UserService userService
    ) {
        this.chatClient = client;
        this.userService = userService;
    }

    @Value("classpath:system_prompts/extract_skills_prompt")
    private Resource sys_prompt_extract_skills;

    @Value("classpath:system_prompts/generate_questions_prompt")
    private Resource sys_prompt_generate_questions;
    @Value("classpath:user_prompts/generate_questions_prompt")
    private Resource user_prompt_generate_questions;

    public String parseResume(MultipartFile file) throws TikaException, IOException {

        Tika tika = new Tika();
        return tika.parseToString(file.getInputStream());
    }

    public List<String> extractSkills(String resumeText) throws TikaException, IOException {
        return chatClient.prompt()
                .system(system -> system.text(sys_prompt_extract_skills)
                        .param("resumeText", resumeText))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }

    public User saveResume(MultipartFile file, String username) throws IOException, TikaException {
        String resumeText = this.parseResume(file);
        List<String> extractedSkills = this.extractSkills(resumeText);

        return userService.updateUserSkills(username, extractedSkills);
    }

    public List<String> fetchAiQuestions(String jobRole, Set<Skill> userSkills) {
        return chatClient.prompt()
                .system(sys -> sys.text(sys_prompt_generate_questions)
                        .params(Map.of(
                                "position", jobRole,
                                "skillSet", userSkills
                        )))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }
}
