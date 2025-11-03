package com.lanki.repository;

import com.lanki.model.ProblemSession;
import com.lanki.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemSessionRepository extends JpaRepository<ProblemSession, Long> {

    List<ProblemSession> findByUserAndStatus(User user, ProblemSession.SessionStatus status);

    @Query("SELECT ps FROM ProblemSession ps WHERE ps.user.id = :userId AND ps.status = 'ACTIVE'")
    List<ProblemSession> findActiveSessionsByUserId(@Param("userId") Long userId);

    Optional<ProblemSession> findByIdAndUser(Long id, User user);

    @Query("SELECT ps FROM ProblemSession ps WHERE ps.user.id = :userId ORDER BY ps.startedAt DESC")
    List<ProblemSession> findRecentSessionsByUserId(@Param("userId") Long userId);

    // Find sessions that have been active for too long (e.g., 2 hours)
    @Query("SELECT ps FROM ProblemSession ps WHERE ps.status = 'ACTIVE' AND ps.startedAt < :cutoffTime")
    List<ProblemSession> findStaleActiveSessions(@Param("cutoffTime") LocalDateTime cutoffTime);
}
