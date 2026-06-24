package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.ResumeMetadata;
import com.deep.skill_drill.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepo extends JpaRepository<ResumeMetadata, Long> {
    Optional<ResumeMetadata> findByUserId(Long userId);
    Optional<ResumeMetadata> findByUser(User user);
    Optional<ResumeMetadata> findByUserUsername(String username);

}
