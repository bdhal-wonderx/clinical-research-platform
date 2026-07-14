package com.wonderx.rwe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ClientPortalResponse {
    private int totalPatients;
    private int completedPatients;
    private BigDecimal totalHonorarium;
    private BigDecimal totalSpend;
    private List<PatientResponse> patients;
}
