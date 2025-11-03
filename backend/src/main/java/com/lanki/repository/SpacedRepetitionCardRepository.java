package com.lanki.repository;

import com.lanki.model.Problem;
import com.lanki.model.SpacedRepetitionCard;
import com.lanki.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpacedRepetitionCardRepository extends JpaRepository<SpacedRepetitionCard, Long> {

    Optional<SpacedRepetitionCard> findByUserAndProblem(User user, Problem problem);

    List<SpacedRepetitionCard> findByUser(User user);

    @Query("SELECT src FROM SpacedRepetitionCard src WHERE src.user.id = :userId AND src.nextReviewDate <= :date ORDER BY src.nextReviewDate ASC")
    List<SpacedRepetitionCard> findDueCardsByUserId(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT src FROM SpacedRepetitionCard src WHERE src.user.id = :userId ORDER BY src.nextReviewDate ASC")
    List<SpacedRepetitionCard> findAllCardsByUserIdOrderedByReviewDate(@Param("userId") Long userId);

    @Query("SELECT COUNT(src) FROM SpacedRepetitionCard src WHERE src.user.id = :userId AND src.nextReviewDate <= :today")
    long countDueCardsByUserId(@Param("userId") Long userId, @Param("today") LocalDate today);
}
