package com.lanki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SessionDTO {
    private Long id;
    private Long problemId;
    private String problemTitle;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String status;
    private Long elapsedTimeSeconds;
    private SubmissionDTO submission;
}
