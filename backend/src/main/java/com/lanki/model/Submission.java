package com.lanki.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Submission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @OneToOne
    @JoinColumn(name = "session_id")
    private ProblemSession session;

    @Column(columnDefinition = "TEXT")
    private String code;

    @Column
    private String language;

    @Column(nullable = false)
    private LocalDateTime submittedAt;

    @Column
    private Integer runtime;

    @Column
    private Double memory;

    @Column
    private String status; // "Accepted", "Wrong Answer", etc.

    @Column
    private String leetcodeSubmissionId; // LeetCode's submission ID

    @OneToOne(mappedBy = "submission", cascade = CascadeType.ALL)
    private Interview interview;
}
