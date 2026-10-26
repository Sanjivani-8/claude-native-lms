package com.lms.assignment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmissionResponse {

    private Long id;
    private Long assignmentId;
    private Long studentId;
    private String content;
    private LocalDateTime submittedAt;
    private Integer score;
    private String feedback;
    private boolean graded;
    private LocalDateTime gradedAt;
    private Long gradedBy;
}
