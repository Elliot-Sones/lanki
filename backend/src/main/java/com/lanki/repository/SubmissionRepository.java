package com.lanki.repository;

import com.lanki.model.Problem;
import com.lanki.model.Submission;
import com.lanki.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    List<Submission> findByUserOrderBySubmittedAtDesc(User user);

    List<Submission> findByUserAndProblemOrderBySubmittedAtDesc(User user, Problem problem);

    Optional<Submission> findByLeetcodeSubmissionId(String leetcodeSubmissionId);

    @Query("SELECT s FROM Submission s WHERE s.user = :user AND s.status = 'Accepted' ORDER BY s.submittedAt DESC")
    List<Submission> findAcceptedSubmissionsByUser(@Param("user") User user);

    @Query("SELECT s FROM Submission s WHERE s.user.id = :userId AND s.submittedAt > :since ORDER BY s.submittedAt DESC")
    List<Submission> findRecentSubmissionsByUserId(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    // Find the latest submission for a user
    Optional<Submission> findFirstByUserOrderBySubmittedAtDesc(User user);
}
