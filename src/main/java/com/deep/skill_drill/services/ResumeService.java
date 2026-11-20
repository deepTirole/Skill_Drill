package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.ResumeMetaDto;
import com.deep.skill_drill.entities.ResumeMetadata;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.exceptions.BadRequestException;
import com.deep.skill_drill.exceptions.ExternalServiceException;
import com.deep.skill_drill.exceptions.ResourceNotFoundException;
import com.deep.skill_drill.repositories.ResumeRepo;
import com.deep.skill_drill.repositories.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class ResumeService {

    private final ChatClient chatClient;
    private final UserService userService;
    private final ResumeRepo resumeRepo;
    private final UserRepo userRepo;

    public ResumeService(
            @Qualifier("googleGenAiChatClient") ChatClient client,
            UserService userService, ResumeRepo resumeRepo, UserRepo userRepo
    ) {
        this.chatClient = client;
        this.userService = userService;
        this.resumeRepo = resumeRepo;
        this.userRepo = userRepo;
    }

    @Value("classpath:system_prompts/extract_skills_prompt.st")
    private Resource sysPromptExtractSkills;

    public String parseResume(MultipartFile file) throws TikaException, IOException {
        Tika tika = new Tika();
        return tika.parseToString(file.getInputStream());
    }

    public List<String> extractSkills(String resumeText) {
        return chatClient.prompt()
                .system(system -> system.text(sysPromptExtractSkills)
                        .param("resumeText", resumeText))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }

    public User saveResume(MultipartFile file, User user) {
        String resumeText;
        try {
            resumeText = this.parseResume(file);
        } catch (TikaException | IOException e) {
            throw new BadRequestException("RESUME_PARSE_FAILED", "The uploaded resume could not be parsed. Please upload a valid PDF", e);
        }

        List<String> extractedSkills;
        try {
            extractedSkills = this.extractSkills(resumeText);
        } catch (Exception e) {
            throw new ExternalServiceException("AI_SKILL_EXTRACTION_FAILED", "Resume skill extraction is temporarily unavailable", e);
        }

        user.getUserSkills().clear();
        userService.updateUserSkills(user.getId(), extractedSkills);

        ResumeMetadata resumeMetadata = resumeRepo.findByUser(user)
                .orElse(new ResumeMetadata());
        resumeMetadata.setFileName(file.getOriginalFilename());
        resumeMetadata.setUser(user);
        resumeMetadata.setUploadTime(LocalDateTime.now());

        resumeRepo.save(resumeMetadata);
        return user;
    }

    public @Nullable ResumeMetaDto getResume(String username) {
        ResumeMetadata resumeMetadata = resumeRepo
                .findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("RESUME_NOT_FOUND", "No resume found for username: " + username));

        ResumeMetaDto resumeMetaDto = new ResumeMetaDto();
        resumeMetaDto.setFilename(resumeMetadata.getFileName());
        resumeMetaDto.setTimestamp(resumeMetadata.getUploadTime());

        return resumeMetaDto;
    }

    public Set<Skill> getUserSkills(String username) {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            throw new ResourceNotFoundException("USER_NOT_FOUND", "User not found: " + username);
        }
        return user.getUserSkills();
    }
}