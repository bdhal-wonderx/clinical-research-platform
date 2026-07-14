package com.wonderx.rwe.service;

import com.wonderx.rwe.entity.FollowupSchedule;
import com.wonderx.rwe.entity.Patient;
import com.wonderx.rwe.entity.StudyReminderRule;
import com.wonderx.rwe.enums.FollowupStatus;
import com.wonderx.rwe.repository.FollowupScheduleRepository;
import com.wonderx.rwe.repository.StudyReminderRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class FollowupService {

    private final FollowupScheduleRepository followupScheduleRepository;
    private final StudyReminderRuleRepository reminderRuleRepository;

    @Transactional
    public FollowupSchedule scheduleFollowup(Patient patient) {
        StudyReminderRule rule = reminderRuleRepository.findByStudyId(patient.getStudy().getId())
                .stream().findFirst().orElse(null);

        int followupDays = rule != null ? rule.getFollowupDays() : 90;
        int windowStartDays = rule != null ? rule.getWindowStartDays() : 80;
        int windowEndDays = rule != null ? rule.getWindowEndDays() : 100;

        LocalDate baseline = LocalDate.now();
        LocalDate due = baseline.plusDays(followupDays);
        LocalDate windowStart = baseline.plusDays(windowStartDays);
        LocalDate windowEnd = baseline.plusDays(windowEndDays);

        return followupScheduleRepository.save(FollowupSchedule.builder()
                .patient(patient)
                .study(patient.getStudy())
                .baselineDate(baseline)
                .dueDate(due)
                .windowStart(windowStart)
                .windowEnd(windowEnd)
                .status(FollowupStatus.SCHEDULED)
                .build());
    }

    @Transactional(readOnly = true)
    public long countOverdueByDoctor(java.util.UUID doctorId, java.util.List<com.wonderx.rwe.entity.Patient> patients) {
        LocalDate today = LocalDate.now();
        return patients.stream()
                .filter(p -> p.getDoctor().getId().equals(doctorId))
                .filter(p -> Boolean.TRUE.equals(p.getBaselineCompleted()) && !Boolean.TRUE.equals(p.getFollowupCompleted()))
                .map(p -> followupScheduleRepository.findByPatientId(p.getId()).orElse(null))
                .filter(f -> f != null && f.getWindowEnd().isBefore(today))
                .count();
    }
}
