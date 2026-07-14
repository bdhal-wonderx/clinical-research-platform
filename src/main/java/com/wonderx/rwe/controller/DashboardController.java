package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.entity.SupportQuery;
import com.wonderx.rwe.service.DashboardService;
import com.wonderx.rwe.service.SupportQueryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboards", description = "Investigator, Ops, Sponsor, Client views")
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private final DashboardService dashboardService;
    private final SupportQueryService supportQueryService;

    @GetMapping("/investigator/me")
    public ResponseEntity<ApiResponse<InvestigatorDashboardResponse>> investigatorDashboard(Authentication auth) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getInvestigatorDashboard(doctorId)));
    }

    @GetMapping("/ops")
    public ResponseEntity<ApiResponse<OpsDashboardResponse>> opsDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getOpsDashboard()));
    }

    @GetMapping("/sponsor/{studyId}")
    public ResponseEntity<ApiResponse<SponsorDashboardResponse>> sponsorDashboard(@PathVariable UUID studyId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getSponsorDashboard(studyId)));
    }

    @GetMapping("/client/{studyId}")
    public ResponseEntity<ApiResponse<ClientPortalResponse>> clientPortal(@PathVariable UUID studyId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getClientPortal(studyId)));
    }

    @PostMapping("/support-queries")
    public ResponseEntity<ApiResponse<SupportQuery>> createSupportQuery(
            Authentication auth, @Valid @RequestBody SupportQueryRequest request) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(supportQueryService.createQuery(doctorId, request)));
    }

    @GetMapping("/support-queries")
    public ResponseEntity<ApiResponse<List<SupportQuery>>> listSupportQueries(Authentication auth) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(supportQueryService.getQueriesByDoctor(doctorId)));
    }
}
