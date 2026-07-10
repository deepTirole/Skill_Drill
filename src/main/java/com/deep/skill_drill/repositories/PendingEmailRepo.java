package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.PendingEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

@Repository
public interface PendingEmailRepo extends JpaRepository<PendingEmail, Integer> {

    PendingEmail findByEmail(String email);
}

