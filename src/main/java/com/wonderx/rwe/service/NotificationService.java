package com.wonderx.rwe.service;

import com.wonderx.rwe.entity.FollowupSchedule;
import com.wonderx.rwe.entity.NotificationLog;
import com.wonderx.rwe.entity.Patient;
import com.wonderx.rwe.repository.FollowupScheduleRepository;
import com.wonderx.rwe.repository.NotificationLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationLogRepository notificationLogRepository;
    private final FollowupScheduleRepository followupScheduleRepository;

    @Transactional
    public void sendSmsReminder(FollowupSchedule schedule, Patient patient) {
        String message = "Reminder: Your TOLERATE-HF follow-up visit is due around "
                + schedule.getDueDate() + ". Please visit your doctor.";
        log.info("SMS to patient {} token {}: {}", patient.getId(), patient.getPatientToken(), message);

        notificationLogRepository.save(NotificationLog.builder()
                .recipientType("PATIENT")
                .recipientId(patient.getId())
                .followupSchedule(schedule)
                .channel("SMS")
                .templateCode("FOLLOWUP_REMINDER")
                .messageBody(message)
                .status("SENT")
                .sentAt(Instant.now())
                .build());

        schedule.setSmsSent(true);
        followupScheduleRepository.save(schedule);
    }

    @Transactional
    public void sendVoiceEscalation(FollowupSchedule schedule, Patient patient) {
        log.info("Voice call to patient {} token {}", patient.getId(), patient.getPatientToken());

        notificationLogRepository.save(NotificationLog.builder()
                .recipientType("PATIENT")
                .recipientId(patient.getId())
                .followupSchedule(schedule)
                .channel("VOICE")
                .templateCode("FOLLOWUP_ESCALATION")
                .messageBody("Automated voice reminder for follow-up visit")
                .status("SENT")
                .sentAt(Instant.now())
                .build());

        schedule.setVoiceSent(true);
        followupScheduleRepository.save(schedule);
    }
}
