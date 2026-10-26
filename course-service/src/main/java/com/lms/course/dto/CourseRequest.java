package com.lms.course.dto;

import com.lms.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequest {

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private Long instructorId;

    private Integer credits;

    private String category;

    private CourseStatus status;
}
