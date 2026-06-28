package com.deep.skill_drill.component;

import com.deep.skill_drill.repositories.PendingRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
public class PendingRegistrationCleanup {

    @Autowired
    private PendingRepo pendingRepo;

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.DAYS)
    @Transactional
    public void deleteExpired() {
        pendingRepo.deleteAllByExpiresAtBefore(LocalDateTime.now());
    }
}
