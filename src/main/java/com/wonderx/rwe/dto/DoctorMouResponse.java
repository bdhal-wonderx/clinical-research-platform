package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.MouStatus;
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
public class DoctorMouResponse {

    private UUID id;
    private UUID studyId;
    private String studyCode;
    private String mouDocumentUrl;
    private Instant mouSignedAt;
    private MouStatus mouStatus;
    private Boolean termsAccepted;
}
