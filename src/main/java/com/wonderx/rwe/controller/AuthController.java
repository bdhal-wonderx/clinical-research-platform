package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based doctor authentication")
public class AuthController {

    private final DoctorService doctorService;

    @PostMapping("/otp/send")
    @Operation(summary = "Send OTP to doctor mobile number")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        doctorService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully", null));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP and receive JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        AuthResponse response = doctorService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
