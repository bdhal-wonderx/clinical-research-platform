package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.DoctorStudyStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorStudyResponse {

    private UUID id;
    private UUID studyId;
    private String studyCode;
    private String studyName;
    private DoctorStudyStatus status;
    private Instant assignedAt;
    private String assignedBy;
}
