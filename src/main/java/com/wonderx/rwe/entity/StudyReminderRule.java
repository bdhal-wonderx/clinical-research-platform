package com.wonderx.rwe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_reminder_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyReminderRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "followup_days", nullable = false)
    @Builder.Default
    private Integer followupDays = 90;

    @Column(name = "window_start_days", nullable = false)
    @Builder.Default
    private Integer windowStartDays = 80;

    @Column(name = "window_end_days", nullable = false)
    @Builder.Default
    private Integer windowEndDays = 100;

    @Column(name = "sms_days_before", nullable = false)
    @Builder.Default
    private Integer smsDaysBefore = 10;

    @Column(name = "voice_escalation_hours", nullable = false)
    @Builder.Default
    private Integer voiceEscalationHours = 48;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
