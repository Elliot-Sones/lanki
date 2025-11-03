package com.lanki.repository;

import com.lanki.model.Interview;
import com.lanki.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    Optional<Interview> findBySubmission(Submission submission);

    @Query("SELECT i FROM Interview i WHERE i.submission.user.id = :userId ORDER BY i.completedAt DESC")
    List<Interview> findByUserIdOrderByCompletedAtDesc(@Param("userId") Long userId);

    @Query("SELECT i FROM Interview i WHERE i.submission.user.id = :userId AND i.status = 'COMPLETED'")
    List<Interview> findCompletedInterviewsByUserId(@Param("userId") Long userId);

    @Query("SELECT AVG(i.overallScore) FROM Interview i WHERE i.submission.user.id = :userId AND i.status = 'COMPLETED'")
    Double findAverageScoreByUserId(@Param("userId") Long userId);
}
