package com.lms.course.dto;

import com.lms.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Detail view of a course, carrying the same fields as {@link CourseResponse}
 * plus the ordered list of modules belonging to the course.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDetailResponse {

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

    @Builder.Default
    private List<ModuleResponse> modules = new ArrayList<>();
}
