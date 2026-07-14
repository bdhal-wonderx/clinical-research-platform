package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.service.PatientService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
@Tag(name = "Patient Registration", description = "Patient enrollment and consent")
@SecurityRequirement(name = "Bearer Authentication")
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    public ResponseEntity<ApiResponse<PatientResponse>> register(
            Authentication auth, @Valid @RequestBody PatientRegistrationRequest request) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(patientService.registerPatient(doctorId, request)));
    }

    @PostMapping(value = "/{patientId}/consent", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PatientResponse>> uploadConsent(
            Authentication auth, @PathVariable UUID patientId, @RequestParam("file") MultipartFile file) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(patientService.uploadConsent(doctorId, patientId, file)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PatientResponse>>> list(Authentication auth) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatientsByDoctor(doctorId)));
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<ApiResponse<PatientResponse>> get(
            Authentication auth, @PathVariable UUID patientId) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(patientService.getPatient(doctorId, patientId)));
    }
}
