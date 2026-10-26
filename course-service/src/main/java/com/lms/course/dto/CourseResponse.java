package com.lms.course.dto;

import com.lms.course.entity.CourseStatus;
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
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Long instructorId;
    private Integer credits;
    private String category;
    private CourseStatus status;
    private int moduleCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
