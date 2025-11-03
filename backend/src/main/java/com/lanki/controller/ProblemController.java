package com.lanki.controller;

import com.lanki.dto.DailyProblemDTO;
import com.lanki.dto.ProblemDTO;
import com.lanki.model.User;
import com.lanki.service.ProblemService;
import com.lanki.service.SpacedRepetitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173") // Allow React dev server
public class ProblemController {

    private final ProblemService problemService;
    private final SpacedRepetitionService spacedRepetitionService;

    /**
     * GET /api/problems
     * Returns all 150 NeetCode problems grouped by category.
     * For the "All Problems" page.
     */
    @GetMapping
    public ResponseEntity<Map<String, List<ProblemDTO>>> getAllProblems(
            @AuthenticationPrincipal User user) {
        Map<String, List<ProblemDTO>> problems = problemService.getProblemsGroupedByCategory(user);
        return ResponseEntity.ok(problems);
    }

    /**
     * GET /api/problems/daily
     * Returns problems due for review today (based on SM-2 algorithm).
     * For the "Daily Problems" homepage.
     */
    @GetMapping("/daily")
    public ResponseEntity<List<DailyProblemDTO>> getDailyProblems(
            @AuthenticationPrincipal User user) {
        List<DailyProblemDTO> dailyProblems = spacedRepetitionService.getDailyRecommendations(user);
        return ResponseEntity.ok(dailyProblems);
    }

    /**
     * GET /api/problems/{id}
     * Get details of a specific problem.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProblemDTO> getProblem(@PathVariable Long id) {
        ProblemDTO problem = ProblemDTO.fromEntity(problemService.getProblemById(id));
        return ResponseEntity.ok(problem);
    }

    /**
     * GET /api/problems/categories
     * Get list of all problem categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        List<String> categories = problemService.getAllCategories();
        return ResponseEntity.ok(categories);
    }
}
