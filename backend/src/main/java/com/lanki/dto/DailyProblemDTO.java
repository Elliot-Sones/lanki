package com.lanki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyProblemDTO {
    private ProblemDTO problem;
    private LocalDate nextReviewDate;
    private Integer daysSinceLastReview;
    private Boolean isOverdue;
    private Integer repetitions;
}
