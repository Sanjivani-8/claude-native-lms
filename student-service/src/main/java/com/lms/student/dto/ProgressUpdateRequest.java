package com.lms.student.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgressUpdateRequest {

    @NotNull(message = "progressPercent is required")
    @Min(value = 0, message = "progressPercent must be >= 0")
    @Max(value = 100, message = "progressPercent must be <= 100")
    private Integer progressPercent;
}
