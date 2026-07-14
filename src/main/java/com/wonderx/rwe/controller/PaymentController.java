package com.wonderx.rwe.controller;

import com.wonderx.rwe.dto.ApiResponse;
import com.wonderx.rwe.entity.Payment;
import com.wonderx.rwe.service.PaymentService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Honorarium tracking and disbursement")
@SecurityRequirement(name = "Bearer Authentication")
public class PaymentController {

    private final PaymentService paymentService;

    @PatchMapping("/{paymentId}/mark-paid")
    public ResponseEntity<ApiResponse<Payment>> markPaid(@PathVariable UUID paymentId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.markPaid(paymentId)));
    }
}
