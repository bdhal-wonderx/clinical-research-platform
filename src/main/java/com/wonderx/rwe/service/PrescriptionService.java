package com.wonderx.rwe.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonderx.rwe.dto.PrescriptionResponse;
import com.wonderx.rwe.entity.*;
import com.wonderx.rwe.enums.*;
import com.wonderx.rwe.exception.BusinessException;
import com.wonderx.rwe.exception.ResourceNotFoundException;
import com.wonderx.rwe.ocr.OcrIntegrationService;
import com.wonderx.rwe.repository.*;
import com.wonderx.rwe.storage.DocumentStorageService;
import com.wonderx.rwe.validation.ProtocolValidationEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionDocumentRepository documentRepository;
    private final PatientVisitRepository visitRepository;
    private final PatientRepository patientRepository;
    private final OcrResultRepository ocrResultRepository;
    private final QcReviewRepository qcReviewRepository;
    private final ValidationResultRepository validationResultRepository;
    private final PatientEcrfRepository ecrfRepository;
    private final DocumentStorageService storageService;
    private final OcrIntegrationService ocrService;
    private final ProtocolValidationEngine validationEngine;
    private final FollowupService followupService;
    private final PaymentService paymentService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    @Transactional
    public PrescriptionResponse uploadPrescription(UUID doctorId, UUID patientId, VisitType visitType, MultipartFile file) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException("Patient does not belong to this doctor");
        }
        if (!Boolean.TRUE.equals(patient.getConsentCaptured())) {
            throw new BusinessException("Consent must be captured before prescription upload");
        }

        PatientVisit visit = visitRepository.findByPatientIdAndVisitType(patientId, visitType).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("Visit not found for type: " + visitType));

        Prescription prescription = prescriptionRepository.findByPatientId(patientId).stream()
                .filter(p -> p.getPatientVisit().getId().equals(visit.getId()))
                .findFirst()
                .orElseGet(() -> prescriptionRepository.save(Prescription.builder()
                        .patientVisit(visit)
                        .patient(patient)
                        .doctor(patient.getDoctor())
                        .study(patient.getStudy())
                        .status(PrescriptionStatus.UPLOADED)
                        .build()));

        if (Boolean.TRUE.equals(prescription.getLocked())) {
            throw new BusinessException("Prescription is locked. Raise a data query to edit.");
        }

        documentRepository.findByPrescriptionIdOrderByVersionNoDesc(prescription.getId())
                .forEach(d -> { d.setSuperseded(true); documentRepository.save(d); });

        int version = documentRepository.findByPrescriptionIdOrderByVersionNoDesc(prescription.getId()).size() + 1;
        String path = storageService.store(file, "prescriptions/" + patientId);

        documentRepository.save(PrescriptionDocument.builder()
                .prescription(prescription)
                .documentUrl(path)
                .fileName(file.getOriginalFilename())
                .mimeType(file.getContentType())
                .versionNo(version)
                .build());

        prescription.setStatus(PrescriptionStatus.OCR_IN_PROGRESS);
        prescriptionRepository.save(prescription);

        processOcr(prescription, path);
        return toResponse(prescription);
    }

    @Transactional
    public void processOcr(Prescription prescription, String documentPath) {
        var result = ocrService.extract(prescription.getId(), documentPath);

        OcrResult ocrResult = ocrResultRepository.findByPrescriptionId(prescription.getId())
                .orElse(OcrResult.builder().prescription(prescription).build());
        ocrResult.setOcrResponse(result.ocrResponse());
        ocrResult.setExtractedData(result.extractedData());
        ocrResult.setConfidenceScore(result.confidenceScore());
        ocrResult.setStatus("OCR_COMPLETED");
        ocrResultRepository.save(ocrResult);

        prescription.setStatus(PrescriptionStatus.OCR_COMPLETED);
        prescriptionRepository.save(prescription);

        ecrfRepository.findByPrescriptionId(prescription.getId()).ifPresentOrElse(
                e -> { e.setEcrfData(result.extractedData()); ecrfRepository.save(e); },
                () -> ecrfRepository.save(PatientEcrf.builder()
                        .prescription(prescription)
                        .patient(prescription.getPatient())
                        .ecrfData(result.extractedData())
                        .build())
        );

        qcReviewRepository.findByPrescriptionId(prescription.getId()).ifPresentOrElse(
                q -> { q.setQcStatus(QcStatus.QC_APPROVED); q.setReviewedData(result.extractedData()); qcReviewRepository.save(q); },
                () -> qcReviewRepository.save(QcReview.builder()
                        .prescription(prescription)
                        .ocrResult(ocrResult)
                        .qcStatus(QcStatus.QC_APPROVED)
                        .reviewedData(result.extractedData())
                        .approvedBy("AUTO")
                        .approvedAt(Instant.now())
                        .build())
        );

        runValidation(prescription);
    }

    @Transactional
    public PrescriptionResponse runValidation(Prescription prescription) {
        String ecrfJson = ecrfRepository.findByPrescriptionId(prescription.getId())
                .map(PatientEcrf::getEcrfData)
                .orElse("{}");

        try {
            JsonNode data = objectMapper.readTree(ecrfJson);
            var outcome = validationEngine.validate(prescription.getStudy().getId(), data);

            validationResultRepository.save(ValidationResult.builder()
                    .prescription(prescription)
                    .patient(prescription.getPatient())
                    .overallStatus(outcome.overallStatus())
                    .ruleResults(outcome.ruleResults().toString())
                    .build());

            prescription.setStatus(switch (outcome.overallStatus()) {
                case PASS -> PrescriptionStatus.VALIDATION_PASSED;
                case FAIL -> PrescriptionStatus.VALIDATION_FAILED;
                case WARNING -> PrescriptionStatus.VALIDATION_WARNING;
            });
            prescriptionRepository.save(prescription);
        } catch (Exception e) {
            throw new BusinessException("Validation failed: " + e.getMessage());
        }
        return toResponse(prescription);
    }

    @Transactional
    public PrescriptionResponse lockPrescription(UUID doctorId, UUID prescriptionId) {
        Prescription prescription = findPrescription(doctorId, prescriptionId);

        if (prescription.getStatus() != PrescriptionStatus.VALIDATION_PASSED
                && prescription.getStatus() != PrescriptionStatus.VALIDATION_WARNING) {
            throw new BusinessException("Prescription must pass validation before locking");
        }

        prescription.setLocked(true);
        prescription.setLockedAt(Instant.now());
        prescription.setStatus(PrescriptionStatus.LOCKED);
        prescriptionRepository.save(prescription);

        Patient patient = prescription.getPatient();
        VisitType visitType = prescription.getPatientVisit().getVisitType();

        if (visitType == VisitType.BASELINE) {
            patient.setBaselineCompleted(true);
            patient.setStatus(PatientStatus.BASELINE_COMPLETED);
            patientRepository.save(patient);
            followupService.scheduleFollowup(patient);
        } else {
            patient.setFollowupCompleted(true);
            patient.setStatus(PatientStatus.COMPLETED);
            patient.setCompletedAt(Instant.now());
            patientRepository.save(patient);
            paymentService.evaluatePayment(patient);
        }

        auditService.log("PRESCRIPTION", prescriptionId, "LOCKED", doctorId.toString(), null, null);
        return toResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getPrescription(UUID doctorId, UUID prescriptionId) {
        return toResponse(findPrescription(doctorId, prescriptionId));
    }

    private Prescription findPrescription(UUID doctorId, UUID prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Prescription not found"));
        if (!prescription.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException("Prescription does not belong to this doctor");
        }
        return prescription;
    }

    private PrescriptionResponse toResponse(Prescription prescription) {
        ValidationStatus valStatus = validationResultRepository.findByPrescriptionId(prescription.getId())
                .stream().findFirst().map(ValidationResult::getOverallStatus).orElse(null);
        String docUrl = documentRepository.findByPrescriptionIdOrderByVersionNoDesc(prescription.getId())
                .stream().filter(d -> !d.getSuperseded()).findFirst()
                .map(PrescriptionDocument::getDocumentUrl).orElse(null);
        String ecrf = ecrfRepository.findByPrescriptionId(prescription.getId())
                .map(PatientEcrf::getEcrfData).orElse(null);
        var confidence = ocrResultRepository.findByPrescriptionId(prescription.getId())
                .map(OcrResult::getConfidenceScore).orElse(null);

        return PrescriptionResponse.builder()
                .id(prescription.getId())
                .patientId(prescription.getPatient().getId())
                .visitType(prescription.getPatientVisit().getVisitType())
                .status(prescription.getStatus())
                .validationStatus(valStatus)
                .confidenceScore(confidence)
                .locked(prescription.getLocked())
                .documentUrl(docUrl)
                .ecrfData(ecrf)
                .lockedAt(prescription.getLockedAt())
                .build();
    }
}
