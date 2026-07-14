package com.wonderx.rwe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyAssignmentRequest {

    @NotNull(message = "Study ID is required")
    private UUID studyId;

    private String assignedBy;
}
