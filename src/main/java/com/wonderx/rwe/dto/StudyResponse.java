package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.StudyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyResponse {

    private UUID id;
    private String studyCode;
    private String studyName;
    private String description;
    private StudyStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Instant createdAt;
}
