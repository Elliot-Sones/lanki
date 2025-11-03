package com.lanki.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "interviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Column(columnDefinition = "TEXT")
    private String transcript; // JSON format of Q&A

    @Column
    private Integer codeScore; // 0-100 based on runtime/memory performance

    @Column
    private Integer explanationScore; // 0-100 based on AI evaluation

    @Column
    private Integer overallScore; // Average of code and explanation

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InterviewStatus status = InterviewStatus.NOT_STARTED;

    public enum InterviewStatus {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    public void calculateOverallScore() {
        if (codeScore != null && explanationScore != null) {
            this.overallScore = (codeScore + explanationScore) / 2;
        }
    }

    /**
     * Converts overall score to SM-2 quality rating (0-5).
     * Used for spaced repetition updates.
     */
    public int toSM2Quality() {
        if (overallScore == null) return 0;
        if (overallScore >= 90) return 5; // Perfect
        if (overallScore >= 80) return 4; // Good
        if (overallScore >= 70) return 3; // Acceptable
        if (overallScore >= 50) return 2; // Hesitant
        if (overallScore >= 30) return 1; // Difficult
        return 0; // Total failure
    }
}
