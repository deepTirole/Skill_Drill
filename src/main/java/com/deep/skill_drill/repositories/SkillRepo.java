package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepo extends JpaRepository<Skill,Integer> {

    Optional<Skill> findBySkillName(String normalizedSkill);
}
