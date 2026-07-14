package com.wonderx.rwe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorPaymentProfileRequest {
    private String bankAccount;
    private String ifscCode;
    private String bankName;
    private String upiId;
    private String accountHolder;
}
