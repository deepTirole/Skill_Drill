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
            @Qualifier("googleGenAiChatClient") ChatClient client,
            @Qualifier("openAiChatClient") ChatClient client2
    ) {
        this.geminiChatClient = client;
        this.openAiChatClient = client2;
    }

    @Value("classpath:user_prompts/gen_ques_user_prompt.st")
    private Resource user_prompt;
    @Value("classpath:system_prompts/generate_questions_prompt.st")
    private Resource sys_prompt_generate_questions;
    @Value("classpath:system_prompts/gen_res_prompt.st")
    private Resource generate_response;
    @Value("classpath:user_prompts/gen_res_user_prompt.st")
    private Resource user_prompt_gen_res;

    // Generating the interview questions.
    public List<String> fetchAiQuestions(String jobRole, String difficulty, User user) {
        Set<Skill> userSkills = user.getUserSkills();
        Integer rating = user.getRating();

        return openAiChatClient.prompt()
                .system(sys -> sys.text(sys_prompt_generate_questions)
                        .params(Map.of("position", jobRole,  "difficulty", difficulty)))
                .user(use -> use.text(user_prompt)
                        .params(Map.of("userSkills",  userSkills,  "userRating", rating)))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }

    // Generating feedback for the give questions and user responses.
    public String generateResponse(QaLog qaLog) {
        return geminiChatClient.prompt()
                .system(sys -> sys.text(generate_response))
                .user(user -> user.text(user_prompt_gen_res)
                        .params(Map.of(
                                "question", qaLog.getQuestion(),
                                "response", qaLog.getUserAnswer() == null ? "no answer provided by user" :
                                        qaLog.getUserAnswer()
                        )))
                .call()
                .content();
    }

//    public List<String> fetchAiQuestionsWithoutRole(Set<Skill> userSkills) {
//        return chatClient.prompt()
//                .system(sys -> sys.text(sys_prompt_generate_questions)
//                        .param("skillSet", userSkills))
//                .call()
//                .entity(new ParameterizedTypeReference<List<String>>() {});
//    }
}
