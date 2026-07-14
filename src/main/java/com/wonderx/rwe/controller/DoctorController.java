package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
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
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
@Tag(name = "Doctor Onboarding", description = "Doctor registration, profile, MOU, and study assignment")
@SecurityRequirement(name = "Bearer Authentication")
public class DoctorController {

    private final DoctorService doctorService;

    @PostMapping("/register")
    @Operation(summary = "Register doctor after OTP verification")
    public ResponseEntity<ApiResponse<DoctorResponse>> register(
            @Valid @RequestBody DoctorRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorService.registerDoctor(request)));
    }

    @PutMapping("/{doctorId}/profile")
    @Operation(summary = "Update doctor profile - medical registration, hospital, specialization")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateProfile(
            @PathVariable UUID doctorId,
            @Valid @RequestBody DoctorProfileRequest request) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.updateProfile(doctorId, request)));
    }

    @PostMapping("/{doctorId}/mou")
    @Operation(summary = "Sign MOU agreement")
    public ResponseEntity<ApiResponse<DoctorMouResponse>> signMou(
            @PathVariable UUID doctorId,
            @Valid @RequestBody MouSignRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorService.signMou(doctorId, request)));
    }

    @PostMapping("/{doctorId}/studies")
    @Operation(summary = "Assign doctor to a study")
    public ResponseEntity<ApiResponse<DoctorStudyResponse>> assignStudy(
            @PathVariable UUID doctorId,
            @Valid @RequestBody StudyAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(doctorService.assignToStudy(doctorId, request)));
    }

    @GetMapping("/{doctorId}")
    @Operation(summary = "Get doctor details with profile, MOU, and study assignments")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctor(@PathVariable UUID doctorId) {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorById(doctorId)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated doctor profile")
    public ResponseEntity<ApiResponse<DoctorResponse>> getCurrentDoctor(Authentication authentication) {
        UUID doctorId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(doctorService.getDoctorById(doctorId)));
    }

    @GetMapping
    @Operation(summary = "List all doctors (operations)")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctors() {
        return ResponseEntity.ok(ApiResponse.success(doctorService.getAllDoctors()));
    }
}
