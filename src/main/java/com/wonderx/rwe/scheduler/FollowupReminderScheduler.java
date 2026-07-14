package com.wonderx.rwe.scheduler;

import com.wonderx.rwe.entity.FollowupSchedule;
import com.wonderx.rwe.entity.Patient;
import com.wonderx.rwe.enums.FollowupStatus;
import com.wonderx.rwe.repository.FollowupScheduleRepository;
import com.wonderx.rwe.repository.StudyReminderRuleRepository;
import com.wonderx.rwe.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowupReminderScheduler {

    private final FollowupScheduleRepository followupScheduleRepository;
    private final StudyReminderRuleRepository reminderRuleRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "${rwe.followup.cron:0 0 8 * * *}")
    @Transactional
    public void processReminders() {
        log.info("Running follow-up reminder scheduler");
        LocalDate today = LocalDate.now();

        for (FollowupSchedule schedule : followupScheduleRepository.findAll()) {
            Patient patient = schedule.getPatient();
            if (Boolean.TRUE.equals(patient.getFollowupCompleted())) {
                continue;
            }

            int smsDaysBefore = reminderRuleRepository.findByStudyId(schedule.getStudy().getId())
                    .stream().findFirst().map(r -> r.getSmsDaysBefore()).orElse(10);

            if (!schedule.getSmsSent() && !today.isBefore(schedule.getDueDate().minusDays(smsDaysBefore))) {
                notificationService.sendSmsReminder(schedule, patient);
                schedule.setStatus(FollowupStatus.SMS_SENT);
                followupScheduleRepository.save(schedule);
            }

            if (today.isAfter(schedule.getWindowEnd())) {
                schedule.setStatus(FollowupStatus.OVERDUE);
                followupScheduleRepository.save(schedule);
                if (!schedule.getVoiceSent()) {
                    notificationService.sendVoiceEscalation(schedule, patient);
                }
            } else if (!today.isBefore(schedule.getWindowStart())) {
                schedule.setStatus(FollowupStatus.DUE);
                followupScheduleRepository.save(schedule);
            }
        }
    }
}
