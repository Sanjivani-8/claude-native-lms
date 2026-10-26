package com.lms.course.dto;

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
public class ModuleResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String content;
    private Integer orderIndex;
    private Integer durationMinutes;
    private LocalDateTime createdAt;
}
