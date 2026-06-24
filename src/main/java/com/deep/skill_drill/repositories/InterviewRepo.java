package com.deep.skill_drill.repositories;

import com.deep.skill_drill.dto.RatingPointDto;
import com.deep.skill_drill.entities.Interview;
import com.deep.skill_drill.entities.QaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterviewRepo extends JpaRepository<Interview,Long> {

    @Query("SELECT new com.deep.skill_drill.dto.RatingPointDto(i.startTime, i.ratingAfter) " +
            "FROM Interview i WHERE i.user.id = :userId AND i.ratingAfter IS NOT NULL " +
            "ORDER BY i.startTime ASC")
    List<RatingPointDto> findRatingHistory(@Param("userId") Long userId);

//    List<QaLog> findAllByInterview_Id(Long interviewId);
}
