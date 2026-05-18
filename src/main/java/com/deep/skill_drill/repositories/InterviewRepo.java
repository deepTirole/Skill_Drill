package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.Interview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepo extends JpaRepository<Interview,Long> {

}
