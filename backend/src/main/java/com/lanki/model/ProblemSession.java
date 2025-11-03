package com.lanki.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * Tracks an active problem-solving session.
 * Created when user clicks "Start" on a problem.
 * Used for real-time polling and timer tracking.
 */
@Entity
@Table(name = "problem_sessions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProblemSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(nullable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SessionStatus status = SessionStatus.ACTIVE;

    @Column
    private Long elapsedTimeSeconds; // Time taken to complete

    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL)
    private Submission submission;

    public enum SessionStatus {
        ACTIVE,      // User is currently solving
        COMPLETED,   // Submission detected
        ABANDONED    // User gave up or timed out
    }

    public void complete(Submission submission) {
        this.completedAt = LocalDateTime.now();
        this.status = SessionStatus.COMPLETED;
        this.submission = submission;
        this.elapsedTimeSeconds = java.time.Duration.between(startedAt, completedAt).getSeconds();
    }
}
