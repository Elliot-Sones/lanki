package com.lanki.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubmissionDTO {
    private Long id;
    private String code;
    private String language;
    private LocalDateTime submittedAt;
    private Integer runtime;
    private Double memory;
    private String status;
}
