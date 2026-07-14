package com.wonderx.rwe.service;

import com.wonderx.rwe.dto.*;
import com.wonderx.rwe.entity.*;
import com.wonderx.rwe.enums.*;
import com.wonderx.rwe.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final DoctorStudyRepository doctorStudyRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final PaymentRepository paymentRepository;
    private final FollowupScheduleRepository followupScheduleRepository;
    private final DoctorMouRepository mouRepository;
    private final SupportQueryRepository supportQueryRepository;
    private final DoctorRepository doctorRepository;
    private final StudyRepository studyRepository;
    private final PatientService patientService;

    @Transactional(readOnly = true)
    public InvestigatorDashboardResponse getInvestigatorDashboard(UUID doctorId) {
        List<Patient> patients = patientRepository.findByDoctorId(doctorId);
        int target = doctorStudyRepository.findByDoctorId(doctorId).stream()
                .mapToInt(DoctorStudy::getPatientAllocation).sum();

        int baseline = (int) patients.stream().filter(p -> Boolean.TRUE.equals(p.getBaselineCompleted())).count();
        int followup = (int) patients.stream().filter(p -> Boolean.TRUE.equals(p.getFollowupCompleted())).count();
        int overdue = countOverdue(patients);

        BigDecimal earned = paymentRepository.sumAmountByDoctorId(doctorId);
        BigDecimal paid = paymentRepository.sumAmountByDoctorIdAndStatus(doctorId, PaymentStatus.PAID);

        return InvestigatorDashboardResponse.builder()
                .enrolled(patients.size())
                .target(target > 0 ? target : 20)
                .baselineCaptured(baseline)
                .followupCaptured(followup)
                .overdueFollowups(overdue)
                .honorariumEarned(earned)
                .honorariumPaid(paid)
                .honorariumPending(earned.subtract(paid))
                .needsAttention(overdue + countIncomplete(patients))
                .patients(patients.stream().map(patientService::toResponse).toList())
                .build();
    }

    @Transactional(readOnly = true)
    public OpsDashboardResponse getOpsDashboard() {
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> allPatients = patientRepository.findAll();
        List<ActionQueueItem> queue = new ArrayList<>();

        followupScheduleRepository.findByDueDateBeforeAndStatus(LocalDate.now(), FollowupStatus.OVERDUE)
                .forEach(f -> queue.add(ActionQueueItem.builder()
                        .type("OVERDUE_FOLLOWUP")
                        .description("Patient " + f.getPatient().getPatientToken() + " follow-up overdue")
                        .priority("HIGH")
                        .entityId(f.getPatient().getId().toString())
                        .build()));

        supportQueryRepository.findByStatus(QueryStatus.OPEN)
                .forEach(q -> queue.add(ActionQueueItem.builder()
                        .type("OPEN_QUERY")
                        .description(q.getSubject())
                        .priority("MEDIUM")
                        .entityId(q.getId().toString())
                        .build()));

        int dormant = (int) doctors.stream()
                .filter(d -> patientRepository.findByDoctorId(d.getId()).isEmpty())
                .count();

        return OpsDashboardResponse.builder()
                .totalInvestigators(doctors.size())
                .activeInvestigators((int) doctors.stream().filter(d -> d.getStatus() == DoctorStatus.ACTIVE).count())
                .totalPatients(allPatients.size())
                .baselineCaptured((int) allPatients.stream().filter(p -> Boolean.TRUE.equals(p.getBaselineCompleted())).count())
                .followupCaptured((int) allPatients.stream().filter(p -> Boolean.TRUE.equals(p.getFollowupCompleted())).count())
                .overdueFollowups(countOverdue(allPatients))
                .unsignedMous((int) doctors.stream().filter(this::hasUnsignedMou).count())
                .openQueries(supportQueryRepository.findByStatus(QueryStatus.OPEN).size())
                .dormantSites(dormant)
                .actionQueue(queue)
                .build();
    }

    @Transactional(readOnly = true)
    public SponsorDashboardResponse getSponsorDashboard(UUID studyId) {
        Study study = studyRepository.findById(studyId)
                .orElseThrow(() -> new com.wonderx.rwe.exception.ResourceNotFoundException("Study not found"));
        List<Patient> patients = patientRepository.findByStudyId(studyId);
        int enrolled = patients.size();
        int baseline = (int) patients.stream().filter(p -> Boolean.TRUE.equals(p.getBaselineCompleted())).count();
        int followup = (int) patients.stream().filter(p -> Boolean.TRUE.equals(p.getFollowupCompleted())).count();

        return SponsorDashboardResponse.builder()
                .targetPatients(study.getTargetPatients() != null ? study.getTargetPatients() : 3000)
                .enrolled(enrolled)
                .baselineCaptured(baseline)
                .followupCaptured(followup)
                .followupCompletionRate(baseline > 0 ? (followup * 100.0 / baseline) : 0)
                .projectedLanding(enrolled > 0 ? (followup * 3000.0 / enrolled) : 0)
                .stabilizedCount(countPhenotype(patients, GdmtPhenotype.STABILIZED))
                .uncontrolledCount(countPhenotype(patients, GdmtPhenotype.UNCONTROLLED))
                .intolerantCount(countPhenotype(patients, GdmtPhenotype.INTOLERANT))
                .build();
    }

    @Transactional(readOnly = true)
    public ClientPortalResponse getClientPortal(UUID studyId) {
        List<Patient> patients = patientRepository.findByStudyId(studyId);
        BigDecimal honorarium = paymentRepository.findAll().stream()
                .filter(p -> p.getStudy().getId().equals(studyId))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ClientPortalResponse.builder()
                .totalPatients(patients.size())
                .completedPatients((int) patients.stream().filter(p -> Boolean.TRUE.equals(p.getFollowupCompleted())).count())
                .totalHonorarium(honorarium)
                .totalSpend(honorarium)
                .patients(patients.stream().map(patientService::toResponse).toList())
                .build();
    }

    private int countOverdue(List<Patient> patients) {
        LocalDate today = LocalDate.now();
        return (int) patients.stream()
                .filter(p -> Boolean.TRUE.equals(p.getBaselineCompleted()) && !Boolean.TRUE.equals(p.getFollowupCompleted()))
                .map(p -> followupScheduleRepository.findByPatientId(p.getId()).orElse(null))
                .filter(f -> f != null && f.getWindowEnd().isBefore(today))
                .count();
    }

    private int countIncomplete(List<Patient> patients) {
        return (int) patients.stream()
                .filter(p -> !Boolean.TRUE.equals(p.getConsentCaptured())
                        || !Boolean.TRUE.equals(p.getBaselineCompleted())
                        || !Boolean.TRUE.equals(p.getFollowupCompleted()))
                .count();
    }

    private boolean hasUnsignedMou(Doctor doctor) {
        return mouRepository.findByDoctorId(doctor.getId()).stream()
                .noneMatch(m -> m.getMouStatus() == MouStatus.SIGNED);
    }

    private int countPhenotype(List<Patient> patients, GdmtPhenotype phenotype) {
        return (int) patients.stream().filter(p -> p.getGdmtPhenotype() == phenotype).count();
    }
}
