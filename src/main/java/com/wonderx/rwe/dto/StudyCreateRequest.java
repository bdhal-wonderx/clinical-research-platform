package com.wonderx.rwe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyCreateRequest {

    @NotBlank(message = "Study code is required")
    private String studyCode;

    @NotBlank(message = "Study name is required")
    private String studyName;

    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
