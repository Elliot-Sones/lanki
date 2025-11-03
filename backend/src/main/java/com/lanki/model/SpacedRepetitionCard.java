package com.lanki.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Implements SuperMemo 2 (SM-2) algorithm for spaced repetition.
 */
@Entity
@Table(name = "spaced_repetition_cards", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "problem_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpacedRepetitionCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // SM-2 Algorithm Parameters
    @Column(nullable = false)
    private Double easeFactor = 2.5; // Default ease factor

    @Column(nullable = false)
    private Integer interval = 1; // Days until next review

    @Column(nullable = false)
    private Integer repetitions = 0; // Number of successful reviews

    @Column(nullable = false)
    private LocalDate nextReviewDate = LocalDate.now();

    @Column
    private LocalDateTime lastReviewDate;

    @Column
    private Integer lastQuality; // Quality of last review (0-5)

    /**
     * Updates the card based on SM-2 algorithm.
     * @param quality Quality rating (0-5): 0=total blackout, 5=perfect response
     */
    public void review(int quality) {
        this.lastReviewDate = LocalDateTime.now();
        this.lastQuality = quality;

        if (quality >= 3) {
            // Correct response
            if (repetitions == 0) {
                interval = 1;
            } else if (repetitions == 1) {
                interval = 6;
            } else {
                interval = (int) Math.round(interval * easeFactor);
            }
            repetitions++;
        } else {
            // Incorrect response - reset
            repetitions = 0;
            interval = 1;
        }

        // Update ease factor
        easeFactor = easeFactor + (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02));

        // Ensure ease factor doesn't go below 1.3
        if (easeFactor < 1.3) {
            easeFactor = 1.3;
        }

        // Set next review date
        this.nextReviewDate = LocalDate.now().plusDays(interval);
    }

    public boolean isDueForReview() {
        return LocalDate.now().isAfter(nextReviewDate) || LocalDate.now().isEqual(nextReviewDate);
    }
}
