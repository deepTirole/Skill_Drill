package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PendingRepo extends JpaRepository<PendingRegistration, String> {
    PendingRegistration findByUsername(String username);

    void deleteAllByExpiresAtBefore(LocalDateTime now);
}
