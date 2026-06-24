package com.deep.skill_drill.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.model.ApiKey;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiConfig {

    @Bean("googleGenAiChatClient")
    @Primary
    public ChatClient flashClient(GoogleGenAiChatModel model) {
        return ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    @Bean("openAiChatClient")
    public ChatClient groqOpenClient(OpenAiChatModel model) {
        return ChatClient.builder(model)
                .build();
    }
}
