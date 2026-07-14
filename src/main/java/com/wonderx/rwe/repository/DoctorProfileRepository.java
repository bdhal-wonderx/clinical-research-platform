package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, UUID> {

    Optional<DoctorProfile> findByDoctorId(UUID doctorId);

    boolean existsByMedicalRegistrationNumber(String medicalRegistrationNumber);
}
