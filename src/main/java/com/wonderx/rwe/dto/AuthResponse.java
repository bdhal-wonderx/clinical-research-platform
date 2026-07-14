package com.wonderx.rwe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String accessToken;
    private String tokenType;
    private Long expiresInMs;
    private String doctorId;
    private String mobileNumber;
    private boolean newDoctor;
}
