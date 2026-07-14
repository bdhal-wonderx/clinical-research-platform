package com.wonderx.rwe.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MouSignRequest {

    private UUID studyId;

    private String mouDocumentUrl;

    private String digitalSignature;

    @NotNull(message = "Terms acceptance is required")
    private Boolean termsAccepted;
}
