package com.wonderx.rwe.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OpsDashboardResponse {
    private int totalInvestigators;
    private int activeInvestigators;
    private int totalPatients;
    private int baselineCaptured;
    private int followupCaptured;
    private int overdueFollowups;
    private int unsignedMous;
    private int openQueries;
    private int dormantSites;
    private List<ActionQueueItem> actionQueue;
}
