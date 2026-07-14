package com.wonderx.rwe.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportQueryRequest {
    @NotBlank private String category;
    @NotBlank private String subject;
    private String detail;
}
