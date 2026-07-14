package com.wonderx.rwe.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class InvestigatorDashboardResponse {
    private int enrolled;
    private int target;
    private int baselineCaptured;
    private int followupCaptured;
    private int overdueFollowups;
    private BigDecimal honorariumEarned;
    private BigDecimal honorariumPaid;
    private BigDecimal honorariumPending;
    private int needsAttention;
    private List<PatientResponse> patients;
}
