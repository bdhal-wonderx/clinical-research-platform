package com.wonderx.rwe.dto;

import com.wonderx.rwe.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class DoctorDocumentSignRequest {
    @NotNull private DocumentType documentType;
    private UUID studyId;
    private String documentUrl;
    private String esignRef;
}
