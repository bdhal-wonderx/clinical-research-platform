package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientRepository extends JpaRepository<Patient, UUID> {

    List<Patient> findByDoctorId(UUID doctorId);

    List<Patient> findByStudyId(UUID studyId);

    Optional<Patient> findByPatientToken(String patientToken);

    long countByDoctorIdAndStudyId(UUID doctorId, UUID studyId);
}
