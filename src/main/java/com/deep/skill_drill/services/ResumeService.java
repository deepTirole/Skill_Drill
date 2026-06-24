package com.deep.skill_drill.services;

import com.deep.skill_drill.dto.ResumeMetaDto;
import com.deep.skill_drill.entities.ResumeMetadata;
import com.deep.skill_drill.entities.Skill;
import com.deep.skill_drill.entities.User;
import com.deep.skill_drill.repositories.ResumeRepo;
import com.deep.skill_drill.repositories.UserRepo;
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
    private Resource sys_prompt_extract_skills;

    @Value("classpath:system_prompts/generate_questions_prompt.st")
    private Resource sys_prompt_generate_questions;
    @Value("classpath:user_prompts/generate_questions_prompt.st")
    private Resource user_prompt_generate_questions;

    @Value("classpath:system_prompts/gen_res_prompt.st")
    private Resource generate_response;
    @Value("classpath:user_prompts/gen_res_user_prompt.st")
    private Resource user_prompt_gen_res;

    //Converting resume doc to plain java string.
    public String parseResume(MultipartFile file) throws TikaException, IOException {

        Tika tika = new Tika();
        return tika.parseToString(file.getInputStream());
    }

    // Extracting technical skills from that plain string.
    public List<String> extractSkills(String resumeText) throws TikaException, IOException {
        return chatClient.prompt()
                .system(system -> system.text(sys_prompt_extract_skills)
                        .param("resumeText", resumeText))
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {});
    }

    // Send those skills to the user repository to save in the database.
    public User saveResume(MultipartFile file, User user) throws IOException, TikaException {
        String resumeText = this.parseResume(file);
        List<String> extractedSkills = this.extractSkills(resumeText);

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
                .findByUserUsername(username).orElse(null);
        if(resumeMetadata == null) {
            return null;
        }

        ResumeMetaDto resumeMetaDto = new ResumeMetaDto();
        resumeMetaDto.setFilename(resumeMetadata.getFileName());
        resumeMetaDto.setTimestamp(resumeMetadata.getUploadTime());

        return resumeMetaDto;
    }

    public Set<Skill> getUserSkills(String username) {
        User user = userRepo.findByUsername(username);
        if(user == null) {
            return null;
        }

        return user.getUserSkills();
    }

}
