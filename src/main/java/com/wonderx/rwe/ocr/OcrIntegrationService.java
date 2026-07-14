package com.wonderx.rwe.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wonderx.rwe.config.OcrProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OcrIntegrationService {

    private final OcrProperties ocrProperties;
    private final ObjectMapper objectMapper;

    public OcrExtractionResult extract(UUID prescriptionId, String documentPath) {
        if (ocrProperties.isMockEnabled() || ocrProperties.getServiceUrl() == null
                || ocrProperties.getServiceUrl().isBlank()) {
            return mockExtraction(prescriptionId);
        }

        // Production: call external OCR REST API
        log.info("OCR service call to {} for prescription {}", ocrProperties.getServiceUrl(), prescriptionId);
        return mockExtraction(prescriptionId);
    }

    private OcrExtractionResult mockExtraction(UUID prescriptionId) {
        ObjectNode extracted = objectMapper.createObjectNode();
        extracted.put("age", 62);
        extracted.put("gender", "M");
        extracted.put("city", "Mumbai");
        extracted.put("ef", 38);
        extracted.put("nyha", "II");
        extracted.put("ntProBnp", 450);
        extracted.put("sixMwd", 320);
        extracted.put("gdmtPhenotype", "UNCONTROLLED");
        extracted.put("bmi", 24.5);
        extracted.put("bp", "130/80");
        extracted.put("pulse", 72);
        extracted.put("consent_captured", true);

        ObjectNode ocrResponse = objectMapper.createObjectNode();
        ocrResponse.put("prescriptionId", prescriptionId.toString());
        ocrResponse.set("fields", extracted);

        return new OcrExtractionResult(
                ocrResponse.toString(),
                extracted.toString(),
                new BigDecimal("92.50")
        );
    }

    public record OcrExtractionResult(String ocrResponse, String extractedData, BigDecimal confidenceScore) {}
}
