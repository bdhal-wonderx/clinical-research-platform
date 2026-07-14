package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.Prescription;
import com.wonderx.rwe.enums.PrescriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {

    List<Prescription> findByPatientId(UUID patientId);

    List<Prescription> findByDoctorId(UUID doctorId);

    List<Prescription> findByStatus(PrescriptionStatus status);
}
