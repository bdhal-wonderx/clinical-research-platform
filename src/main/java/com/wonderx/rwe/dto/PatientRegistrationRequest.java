package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.GdmtPhenotype;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class PatientRegistrationRequest {
    @NotNull private UUID studyId;
    @Min(18) @Max(120) private Integer age;
    private String gender;
    private String city;
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid mobile number") private String mobileNumber;
    private Boolean smoking;
    private Boolean alcohol;
    private GdmtPhenotype gdmtPhenotype;
}
