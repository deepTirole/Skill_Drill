package com.deep.skill_drill.repositories;

import com.deep.skill_drill.entities.QaLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QaLogsRepo extends JpaRepository<QaLog, Long> {

}
