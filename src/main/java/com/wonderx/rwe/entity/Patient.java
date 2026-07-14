package com.wonderx.rwe.entity;

import com.wonderx.rwe.enums.GdmtPhenotype;
import com.wonderx.rwe.enums.PatientStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patient")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_id", nullable = false)
    private Study study;

    @Column(name = "patient_token", nullable = false, unique = true, length = 50)
    private String patientToken;

    private Integer age;

    @Column(length = 20)
    private String gender;

    @Column(length = 100)
    private String city;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    private Boolean smoking;

    private Boolean alcohol;

    @Enumerated(EnumType.STRING)
    @Column(name = "gdmt_phenotype", length = 30)
    private GdmtPhenotype gdmtPhenotype;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private PatientStatus status = PatientStatus.REGISTERED;

    @Column(name = "consent_captured", nullable = false)
    @Builder.Default
    private Boolean consentCaptured = false;

    @Column(name = "baseline_completed", nullable = false)
    @Builder.Default
    private Boolean baselineCompleted = false;

    @Column(name = "followup_completed", nullable = false)
    @Builder.Default
    private Boolean followupCompleted = false;

    @Column(name = "enrolled_at", nullable = false)
    @Builder.Default
    private Instant enrolledAt = Instant.now();

    @Column(name = "completed_at")
    private Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
