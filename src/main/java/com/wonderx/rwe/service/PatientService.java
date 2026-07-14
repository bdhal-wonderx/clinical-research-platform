package com.wonderx.rwe.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wonderx.rwe.dto.PatientRegistrationRequest;
import com.wonderx.rwe.dto.PatientResponse;
import com.wonderx.rwe.entity.*;
import com.wonderx.rwe.enums.*;
import com.wonderx.rwe.exception.BusinessException;
import com.wonderx.rwe.exception.ResourceNotFoundException;
import com.wonderx.rwe.repository.*;
import com.wonderx.rwe.storage.DocumentStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientConsentRepository consentRepository;
    private final PatientVisitRepository visitRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorStudyRepository doctorStudyRepository;
    private final StudyRepository studyRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final DocumentStorageService storageService;
    private final AuditService auditService;

    @Transactional
    public PatientResponse registerPatient(UUID doctorId, PatientRegistrationRequest request) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found"));
        Study study = studyRepository.findById(request.getStudyId())
                .orElseThrow(() -> new ResourceNotFoundException("Study not found"));

        DoctorStudy assignment = doctorStudyRepository.findByDoctorIdAndStudyId(doctorId, request.getStudyId())
                .orElseThrow(() -> new BusinessException("Doctor not assigned to this study"));

        long enrolled = patientRepository.countByDoctorIdAndStudyId(doctorId, request.getStudyId());
        if (enrolled >= assignment.getPatientAllocation()) {
            throw new BusinessException("Patient allocation limit reached (" + assignment.getPatientAllocation() + ")");
        }

        String token = "THF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Patient patient = Patient.builder()
                .doctor(doctor)
                .study(study)
                .patientToken(token)
                .age(request.getAge())
                .gender(request.getGender())
                .city(request.getCity())
                .mobileNumber(request.getMobileNumber())
                .smoking(request.getSmoking())
                .alcohol(request.getAlcohol())
                .gdmtPhenotype(request.getGdmtPhenotype())
                .status(PatientStatus.REGISTERED)
                .build();

        patientRepository.save(patient);

        visitRepository.save(PatientVisit.builder()
                .patient(patient).visitType(VisitType.BASELINE).visitNumber(1).status("PENDING").build());
        visitRepository.save(PatientVisit.builder()
                .patient(patient).visitType(VisitType.FOLLOWUP).visitNumber(2).status("PENDING").build());

        assignment.setPatientsEnrolled((int) enrolled + 1);
        doctorStudyRepository.save(assignment);

        auditService.log("PATIENT", patient.getId(), "REGISTERED", doctor.getMobileNumber(), null, null);
        return toResponse(patient);
    }

    @Transactional
    public PatientResponse uploadConsent(UUID doctorId, UUID patientId, MultipartFile file) {
        Patient patient = findPatientForDoctor(doctorId, patientId);
        Doctor doctor = patient.getDoctor();
        String path = storageService.store(file, "consent/" + patientId);

        consentRepository.findByPatientId(patientId).ifPresentOrElse(
                c -> { c.setConsentImageUrl(path); consentRepository.save(c); },
                () -> consentRepository.save(PatientConsent.builder()
                        .patient(patient)
                        .consentImageUrl(path)
                        .capturedBy(doctor)
                        .build())
        );

        patient.setConsentCaptured(true);
        patientRepository.save(patient);
        auditService.log("PATIENT", patientId, "CONSENT_CAPTURED", doctorId.toString(), null, null);
        return toResponse(patient);
    }

    @Transactional(readOnly = true)
    public List<PatientResponse> getPatientsByDoctor(UUID doctorId) {
        return patientRepository.findByDoctorId(doctorId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public PatientResponse getPatient(UUID doctorId, UUID patientId) {
        return toResponse(findPatientForDoctor(doctorId, patientId));
    }

    private Patient findPatientForDoctor(UUID doctorId, UUID patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found"));
        if (!patient.getDoctor().getId().equals(doctorId)) {
            throw new BusinessException("Patient does not belong to this doctor");
        }
        return patient;
    }

    private String visitPrescriptionStatus(UUID patientId, VisitType visitType) {
        return visitRepository.findByPatientIdAndVisitType(patientId, visitType).stream()
                .findFirst()
                .map(v -> prescriptionRepository.findByPatientId(patientId).stream()
                        .filter(p -> p.getPatientVisit().getId().equals(v.getId()))
                        .findFirst().map(pr -> pr.getStatus().name()).orElse("PENDING"))
                .orElse("PENDING");
    }

    PatientResponse toResponse(Patient patient) {
        return PatientResponse.builder()
                .id(patient.getId())
                .patientToken(patient.getPatientToken())
                .studyId(patient.getStudy().getId())
                .age(patient.getAge())
                .gender(patient.getGender())
                .city(patient.getCity())
                .gdmtPhenotype(patient.getGdmtPhenotype())
                .status(patient.getStatus())
                .consentCaptured(patient.getConsentCaptured())
                .baselineCompleted(patient.getBaselineCompleted())
                .followupCompleted(patient.getFollowupCompleted())
                .enrolledAt(patient.getEnrolledAt())
                .baselineStatus(visitPrescriptionStatus(patient.getId(), VisitType.BASELINE))
                .followupStatus(visitPrescriptionStatus(patient.getId(), VisitType.FOLLOWUP))
                .build();
    }
}
