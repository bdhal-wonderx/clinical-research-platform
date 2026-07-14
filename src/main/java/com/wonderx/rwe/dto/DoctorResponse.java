package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.DoctorStatus;
import com.wonderx.rwe.enums.MouStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorResponse {

    private UUID id;
    private String mobileNumber;
    private String email;
    private String firstName;
    private String lastName;
    private DoctorStatus status;
    private Instant otpVerifiedAt;
    private Instant createdAt;
    private DoctorProfileResponse profile;
    private List<DoctorMouResponse> mouAgreements;
    private List<DoctorStudyResponse> studyAssignments;
}
