package com.wonderx.rwe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "doctor_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false, unique = true)
    private Doctor doctor;

    @Column(name = "medical_registration_number", nullable = false, length = 100)
    private String medicalRegistrationNumber;

    @Column(name = "registration_council", nullable = false, length = 100)
    private String registrationCouncil;

    @Column(name = "registration_year")
    private Integer registrationYear;

    @Column(name = "hospital_name", nullable = false, length = 255)
    private String hospitalName;

    @Column(name = "hospital_address", columnDefinition = "TEXT")
    private String hospitalAddress;

    @Column(name = "hospital_city", length = 100)
    private String hospitalCity;

    @Column(name = "hospital_state", length = 100)
    private String hospitalState;

    @Column(name = "hospital_pincode", length = 10)
    private String hospitalPincode;

    @Column(nullable = false, length = 150)
    private String specialization;

    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
