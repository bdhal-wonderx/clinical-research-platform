package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.ApiResponse;
import com.wonderx.rwe.dto.StudyCreateRequest;
import com.wonderx.rwe.dto.StudyResponse;
import com.wonderx.rwe.service.StudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/studies")
@RequiredArgsConstructor
@Tag(name = "Study Configuration", description = "Study management for doctor assignment")
@SecurityRequirement(name = "Bearer Authentication")
public class StudyController {

    private final StudyService studyService;

    @PostMapping
    @Operation(summary = "Create a new study")
    public ResponseEntity<ApiResponse<StudyResponse>> createStudy(
            @Valid @RequestBody StudyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(studyService.createStudy(request)));
    }

    @GetMapping("/{studyId}")
    @Operation(summary = "Get study by ID")
    public ResponseEntity<ApiResponse<StudyResponse>> getStudy(@PathVariable UUID studyId) {
        return ResponseEntity.ok(ApiResponse.success(studyService.getStudyById(studyId)));
    }

    @GetMapping("/active")
    @Operation(summary = "List active studies available for doctor assignment")
    public ResponseEntity<ApiResponse<List<StudyResponse>>> getActiveStudies() {
        return ResponseEntity.ok(ApiResponse.success(studyService.getActiveStudies()));
    }

    @GetMapping
    @Operation(summary = "List all studies")
    public ResponseEntity<ApiResponse<List<StudyResponse>>> getAllStudies() {
        return ResponseEntity.ok(ApiResponse.success(studyService.getAllStudies()));
    }

    @PatchMapping("/{studyId}/activate")
    @Operation(summary = "Activate a study")
    public ResponseEntity<ApiResponse<StudyResponse>> activateStudy(@PathVariable UUID studyId) {
        return ResponseEntity.ok(ApiResponse.success(studyService.activateStudy(studyId)));
    }
}
