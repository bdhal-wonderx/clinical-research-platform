package com.wonderx.rwe.dto;

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
public class DoctorProfileResponse {

    private UUID id;
    private String medicalRegistrationNumber;
    private String registrationCouncil;
    private Integer registrationYear;
    private String hospitalName;
    private String hospitalAddress;
    private String hospitalCity;
    private String hospitalState;
    private String hospitalPincode;
    private String specialization;
    private Integer yearsOfExperience;
    private Instant createdAt;
}
