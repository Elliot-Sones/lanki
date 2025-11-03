package com.lanki.dto;

import com.lanki.model.Problem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemDTO {
    private Long id;
    private String title;
    private String titleSlug;
    private String category;
    private String difficulty;
    private String leetcodeUrl;
    private Boolean isPremium;
    private String progressStatus; // For user's progress

    public static ProblemDTO fromEntity(Problem problem) {
        return new ProblemDTO(
            problem.getId(),
            problem.getTitle(),
            problem.getTitleSlug(),
            problem.getCategory(),
            problem.getDifficulty().name(),
            problem.getLeetcodeUrl(),
            problem.getIsPremium(),
            null
        );
    }
}
