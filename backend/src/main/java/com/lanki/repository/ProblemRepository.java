package com.lanki.repository;

import com.lanki.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    Optional<Problem> findByTitleSlug(String titleSlug);

    List<Problem> findByCategory(String category);

    @Query("SELECT DISTINCT p.category FROM Problem p ORDER BY p.category")
    List<String> findAllCategories();

    List<Problem> findByDifficulty(Problem.Difficulty difficulty);

    @Query("SELECT p FROM Problem p WHERE p.isPremium = false ORDER BY p.id")
    List<Problem> findAllFreeProblems();
}
