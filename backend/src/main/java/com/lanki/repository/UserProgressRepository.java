package com.lanki.repository;

import com.lanki.model.Problem;
import com.lanki.model.User;
import com.lanki.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findByUserAndProblem(User user, Problem problem);

    List<UserProgress> findByUser(User user);

    List<UserProgress> findByUserAndStatus(User user, UserProgress.ProgressStatus status);

    @Query("SELECT up FROM UserProgress up WHERE up.user.id = :userId AND up.status = 'COMPLETED'")
    List<UserProgress> findCompletedProblemsByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(up) FROM UserProgress up WHERE up.user.id = :userId AND up.status = 'COMPLETED'")
    long countCompletedProblemsByUserId(@Param("userId") Long userId);

    @Query("SELECT up FROM UserProgress up WHERE up.user.id = :userId AND up.problem.category = :category")
    List<UserProgress> findByUserIdAndCategory(@Param("userId") Long userId, @Param("category") String category);
}
