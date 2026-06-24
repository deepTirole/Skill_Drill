package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QaLogsRepo extends JpaRepository<QaLog, Long> {

    List<QaLog> findAllByInterviewId(Long id);

}
