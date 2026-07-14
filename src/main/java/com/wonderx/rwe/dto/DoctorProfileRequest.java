package com.wonderx.rwe.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfileRequest {

    @NotBlank(message = "Medical registration number is required")
    private String medicalRegistrationNumber;

    @NotBlank(message = "Registration council is required")
    private String registrationCouncil;

    @Min(value = 1950, message = "Invalid registration year")
    @Max(value = 2100, message = "Invalid registration year")
    private Integer registrationYear;

    @NotBlank(message = "Hospital name is required")
    private String hospitalName;

    private String hospitalAddress;

    private String hospitalCity;

    private String hospitalState;

    @Pattern(regexp = "^\\d{6}$", message = "Pincode must be 6 digits")
    private String hospitalPincode;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @Min(value = 0, message = "Years of experience cannot be negative")
    @Max(value = 60, message = "Invalid years of experience")
    private Integer yearsOfExperience;
}
