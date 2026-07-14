package com.wonderx.rwe.service;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.entity.*;
import com.wonderx.rwe.enums.*;
import com.wonderx.rwe.exception.BusinessException;
import com.wonderx.rwe.exception.ResourceNotFoundException;
import com.wonderx.rwe.mapper.DoctorMapper;
import com.wonderx.rwe.repository.*;
import com.wonderx.rwe.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorMouRepository doctorMouRepository;
    private final DoctorStudyRepository doctorStudyRepository;
    private final StudyRepository studyRepository;
    private final DoctorPaymentProfileRepository paymentProfileRepository;
    private final DoctorDocumentRepository documentRepository;
    private final DoctorMapper doctorMapper;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final AuditService auditService;

    @Transactional
    public void sendOtp(OtpSendRequest request) {
        otpService.sendOtp(request.getMobileNumber());

        doctorRepository.findByMobileNumber(request.getMobileNumber())
                .orElseGet(() -> doctorRepository.save(Doctor.builder()
                        .mobileNumber(request.getMobileNumber())
                        .status(DoctorStatus.PENDING_OTP)
                        .build()));
    }

    @Transactional
    public AuthResponse verifyOtp(OtpVerifyRequest request) {
        otpService.verifyOtp(request.getMobileNumber(), request.getOtp());

        Doctor doctor = doctorRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found for mobile: " + request.getMobileNumber()));

        boolean isNew = doctor.getStatus() == DoctorStatus.PENDING_OTP;

        if (doctor.getStatus() == DoctorStatus.PENDING_OTP) {
            doctor.setStatus(DoctorStatus.OTP_VERIFIED);
            doctor.setOtpVerifiedAt(Instant.now());
            doctorRepository.save(doctor);
            auditService.log("DOCTOR", doctor.getId(), "OTP_VERIFIED", doctor.getMobileNumber(), null, null);
        }

        String token = jwtService.generateToken(doctor.getId(), doctor.getMobileNumber());

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .expiresInMs(86400000L)
                .doctorId(doctor.getId().toString())
                .mobileNumber(doctor.getMobileNumber())
                .newDoctor(isNew)
                .build();
    }

    @Transactional
    public DoctorResponse registerDoctor(DoctorRegistrationRequest request) {
        Doctor doctor = doctorRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found. Complete OTP verification first."));

        if (doctor.getStatus() == DoctorStatus.PENDING_OTP) {
            throw new BusinessException("OTP verification required before registration");
        }

        doctor.setFirstName(request.getFirstName());
        doctor.setLastName(request.getLastName());
        doctor.setEmail(request.getEmail());
        doctorRepository.save(doctor);

        auditService.log("DOCTOR", doctor.getId(), "REGISTERED", doctor.getMobileNumber(), null, null);
        return getDoctorById(doctor.getId());
    }

    @Transactional
    public DoctorResponse updateProfile(UUID doctorId, DoctorProfileRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        if (doctorProfileRepository.existsByMedicalRegistrationNumber(request.getMedicalRegistrationNumber())) {
            var existingProfile = doctorProfileRepository.findByDoctorId(doctorId);
            boolean isOwnRegistration = existingProfile
                    .map(p -> p.getMedicalRegistrationNumber().equals(request.getMedicalRegistrationNumber()))
                    .orElse(false);
            if (!isOwnRegistration) {
                throw new BusinessException("Medical registration number already exists");
            }
        }

        DoctorProfile profile = doctorProfileRepository.findByDoctorId(doctorId)
                .orElse(DoctorProfile.builder().doctor(doctor).build());

        profile.setMedicalRegistrationNumber(request.getMedicalRegistrationNumber());
        profile.setRegistrationCouncil(request.getRegistrationCouncil());
        profile.setRegistrationYear(request.getRegistrationYear());
        profile.setHospitalName(request.getHospitalName());
        profile.setHospitalAddress(request.getHospitalAddress());
        profile.setHospitalCity(request.getHospitalCity());
        profile.setHospitalState(request.getHospitalState());
        profile.setHospitalPincode(request.getHospitalPincode());
        profile.setSpecialization(request.getSpecialization());
        profile.setYearsOfExperience(request.getYearsOfExperience());

        doctorProfileRepository.save(profile);

        if (doctor.getStatus() == DoctorStatus.OTP_VERIFIED) {
            doctor.setStatus(DoctorStatus.PROFILE_COMPLETED);
            doctorRepository.save(doctor);
        }

        auditService.log("DOCTOR_PROFILE", profile.getId(), "UPDATED", doctor.getMobileNumber(), null, null);
        return getDoctorById(doctorId);
    }

    @Transactional
    public DoctorMouResponse signMou(UUID doctorId, MouSignRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        if (doctor.getStatus().ordinal() < DoctorStatus.PROFILE_COMPLETED.ordinal()) {
            throw new BusinessException("Complete profile before signing MOU");
        }

        if (!Boolean.TRUE.equals(request.getTermsAccepted())) {
            throw new BusinessException("Terms and conditions must be accepted");
        }

        Study study = null;
        if (request.getStudyId() != null) {
            study = studyRepository.findById(request.getStudyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Study not found: " + request.getStudyId()));
        }

        DoctorMou mou = DoctorMou.builder()
                .doctor(doctor)
                .study(study)
                .mouDocumentUrl(request.getMouDocumentUrl())
                .digitalSignature(request.getDigitalSignature())
                .termsAccepted(true)
                .mouStatus(MouStatus.SIGNED)
                .mouSignedAt(Instant.now())
                .build();

        doctorMouRepository.save(mou);

        if (doctor.getStatus() == DoctorStatus.PROFILE_COMPLETED) {
            doctor.setStatus(DoctorStatus.MOU_SIGNED);
            doctorRepository.save(doctor);
        }

        auditService.log("DOCTOR_MOU", mou.getId(), "SIGNED", doctor.getMobileNumber(), null, null);
        return doctorMapper.toMouResponse(mou);
    }

    @Transactional
    public DoctorStudyResponse assignToStudy(UUID doctorId, StudyAssignmentRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);

        if (doctor.getStatus().ordinal() < DoctorStatus.MOU_SIGNED.ordinal()) {
            throw new BusinessException("MOU must be signed before study assignment");
        }

        Study study = studyRepository.findById(request.getStudyId())
                .orElseThrow(() -> new ResourceNotFoundException("Study not found: " + request.getStudyId()));

        if (study.getStatus() != StudyStatus.ACTIVE) {
            throw new BusinessException("Study is not active");
        }

        if (doctorStudyRepository.existsByDoctorIdAndStudyIdAndStatus(
                doctorId, request.getStudyId(), DoctorStudyStatus.ACTIVE)) {
            throw new BusinessException("Doctor is already assigned to this study");
        }

        DoctorStudy assignment = DoctorStudy.builder()
                .doctor(doctor)
                .study(study)
                .assignedBy(request.getAssignedBy())
                .status(DoctorStudyStatus.ACTIVE)
                .patientAllocation(20)
                .patientsEnrolled(0)
                .assignedAt(Instant.now())
                .build();

        doctorStudyRepository.save(assignment);

        doctor.setStatus(DoctorStatus.ACTIVE);
        doctorRepository.save(doctor);

        auditService.log("DOCTOR_STUDY", assignment.getId(), "ASSIGNED", doctor.getMobileNumber(), null, null);
        return doctorMapper.toStudyAssignmentResponse(assignment);
    }

    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(UUID doctorId) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        DoctorResponse response = doctorMapper.toResponse(doctor);

        doctorProfileRepository.findByDoctorId(doctorId)
                .ifPresent(p -> response.setProfile(doctorMapper.toProfileResponse(p)));

        response.setMouAgreements(doctorMapper.toMouResponseList(doctorMouRepository.findByDoctorId(doctorId)));
        response.setStudyAssignments(doctorMapper.toStudyAssignmentResponseList(
                doctorStudyRepository.findByDoctorId(doctorId)));

        return response;
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctors() {
        return doctorRepository.findAll().stream()
                .map(d -> getDoctorById(d.getId()))
                .toList();
    }

    @Transactional
    public void updatePaymentProfile(UUID doctorId, DoctorPaymentProfileRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        DoctorPaymentProfile profile = paymentProfileRepository.findByDoctorId(doctorId)
                .orElse(DoctorPaymentProfile.builder().doctor(doctor).build());
        profile.setBankAccount(request.getBankAccount());
        profile.setIfscCode(request.getIfscCode());
        profile.setBankName(request.getBankName());
        profile.setUpiId(request.getUpiId());
        profile.setAccountHolder(request.getAccountHolder());
        paymentProfileRepository.save(profile);
        auditService.log("DOCTOR_PAYMENT_PROFILE", profile.getId(), "UPDATED", doctor.getMobileNumber(), null, null);
    }

    @Transactional
    public void signDocument(UUID doctorId, DoctorDocumentSignRequest request) {
        Doctor doctor = findDoctorOrThrow(doctorId);
        Study study = request.getStudyId() != null
                ? studyRepository.findById(request.getStudyId()).orElse(null) : null;

        DoctorDocument doc = documentRepository.findByDoctorId(doctorId).stream()
                .filter(d -> d.getDocumentType() == request.getDocumentType())
                .filter(d -> request.getStudyId() == null
                        || (d.getStudy() != null && d.getStudy().getId().equals(request.getStudyId())))
                .findFirst()
                .orElse(DoctorDocument.builder()
                        .doctor(doctor)
                        .study(study)
                        .documentType(request.getDocumentType())
                        .build());

        doc.setDocumentUrl(request.getDocumentUrl());
        doc.setEsignRef(request.getEsignRef());
        doc.setStatus("SIGNED");
        doc.setSignedAt(Instant.now());
        documentRepository.save(doc);
        auditService.log("DOCTOR_DOCUMENT", doc.getId(), "SIGNED", doctor.getMobileNumber(), null, null);
    }

    @Transactional(readOnly = true)
    public boolean isOnboardingComplete(UUID doctorId) {
        return documentRepository.findByDoctorId(doctorId).stream()
                .filter(d -> "SIGNED".equals(d.getStatus()))
                .count() >= 1
                && doctorMouRepository.existsByDoctorIdAndMouStatus(doctorId, MouStatus.SIGNED);
    }

    private Doctor findDoctorOrThrow(UUID doctorId) {
        return doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found: " + doctorId));
    }
}
