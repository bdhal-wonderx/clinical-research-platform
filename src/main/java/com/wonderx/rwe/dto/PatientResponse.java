package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.GdmtPhenotype;
import com.wonderx.rwe.enums.PatientStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PatientResponse {
    private UUID id;
    private String patientToken;
    private UUID studyId;
    private Integer age;
    private String gender;
    private String city;
    private GdmtPhenotype gdmtPhenotype;
    private PatientStatus status;
    private Boolean consentCaptured;
    private Boolean baselineCompleted;
    private Boolean followupCompleted;
    private Instant enrolledAt;
    private String baselineStatus;
    private String followupStatus;
}
