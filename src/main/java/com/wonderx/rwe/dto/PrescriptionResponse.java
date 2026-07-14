package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.PrescriptionStatus;
import com.wonderx.rwe.enums.ValidationStatus;
import com.wonderx.rwe.enums.VisitType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PrescriptionResponse {
    private UUID id;
    private UUID patientId;
    private VisitType visitType;
    private PrescriptionStatus status;
    private ValidationStatus validationStatus;
    private BigDecimal confidenceScore;
    private Boolean locked;
    private String documentUrl;
    private String ecrfData;
    private String validationGaps;
    private Instant lockedAt;
}
