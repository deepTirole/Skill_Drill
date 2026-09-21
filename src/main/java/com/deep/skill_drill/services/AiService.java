package com.deep.skill_drill.services;

import com.deep.skill_drill.entities.QaLog;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AiService {

    private final ChatClient geminiChatClient;
    private final ChatClient openAiChatClient;

    public AiService(
            @Qualifier("googleGenAiChatClient") ChatClient geminiClient,
            @Qualifier("openAiChatClient") ChatClient openAiClient
    ) {
        this.geminiChatClient = geminiClient;
        this.openAiChatClient = openAiClient;
    }

    @Value("classpath:user_prompts/gen_ques_user_prompt.st")
    private Resource userPrompt;

    @Value("classpath:system_prompts/generate_questions_prompt.st")
    private Resource sysPromptGenerateQuestions;

    @Value("classpath:system_prompts/gen_res_prompt.st")
    private Resource generateResponse;

    @Value("classpath:user_prompts/gen_res_user_prompt.st")
    private Resource userPromptGenRes;

    public List<String> fetchAiQuestions(String jobRole, String difficulty, User user) {
        Set<Skill> userSkills = user.getUserSkills();
        Integer rating = user.getRating();

        return openAiChatClient.prompt()
                .system(sys -> sys.text(sysPromptGenerateQuestions)
                        .params(Map.of("position", jobRole, "difficulty", difficulty)))
                .user(use -> use.text(userPrompt)
                        .params(Map.of("userSkills", userSkills, "userRating", rating)))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }

    public String generateResponse(QaLog qaLog) {
        return geminiChatClient.prompt()
                .system(sys -> sys.text(generateResponse))
                .user(user -> user.text(userPromptGenRes)
                        .params(Map.of(
                                "question", qaLog.getQuestion(),
                                "response", qaLog.getUserAnswer() == null ? "Candidate provided no answer." : qaLog.getUserAnswer()
                        )))
                .call()
                .content();
    }
}