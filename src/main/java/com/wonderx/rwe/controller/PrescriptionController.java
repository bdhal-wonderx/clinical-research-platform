package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.ApiResponse;
import com.wonderx.rwe.dto.PrescriptionResponse;
import com.wonderx.rwe.enums.VisitType;
import com.wonderx.rwe.repository.PrescriptionRepository;
import com.wonderx.rwe.service.PrescriptionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
@Tag(name = "Prescription Capture", description = "Rx upload, OCR, validation, lock")
@SecurityRequirement(name = "Bearer Authentication")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PrescriptionRepository prescriptionRepository;

    @PostMapping(value = "/patients/{patientId}/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PrescriptionResponse>> upload(
            Authentication auth, @PathVariable UUID patientId,
            @RequestParam VisitType visitType, @RequestParam("file") MultipartFile file) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(prescriptionService.uploadPrescription(doctorId, patientId, visitType, file)));
    }

    @GetMapping("/{prescriptionId}")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> get(
            Authentication auth, @PathVariable UUID prescriptionId) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.getPrescription(doctorId, prescriptionId)));
    }

    @PostMapping("/{prescriptionId}/lock")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> lock(
            Authentication auth, @PathVariable UUID prescriptionId) {
        UUID doctorId = UUID.fromString(auth.getName());
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.lockPrescription(doctorId, prescriptionId)));
    }

    @PostMapping("/{prescriptionId}/validate")
    public ResponseEntity<ApiResponse<PrescriptionResponse>> validate(
            Authentication auth, @PathVariable UUID prescriptionId) {
        UUID doctorId = UUID.fromString(auth.getName());
        var prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new com.wonderx.rwe.exception.ResourceNotFoundException("Prescription not found"));
        if (!prescription.getDoctor().getId().equals(doctorId)) {
            throw new com.wonderx.rwe.exception.BusinessException("Access denied");
        }
        return ResponseEntity.ok(ApiResponse.success(prescriptionService.runValidation(prescription)));
    }
}
