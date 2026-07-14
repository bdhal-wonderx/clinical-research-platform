package com.wonderx.rwe.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SponsorDashboardResponse {
    private int targetPatients;
    private int enrolled;
    private int baselineCaptured;
    private int followupCaptured;
    private double followupCompletionRate;
    private double projectedLanding;
    private int stabilizedCount;
    private int uncontrolledCount;
    private int intolerantCount;
}
