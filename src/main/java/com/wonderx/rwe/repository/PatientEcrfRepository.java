package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.PatientEcrf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientEcrfRepository extends JpaRepository<PatientEcrf, UUID> {

    Optional<PatientEcrf> findByPrescriptionId(UUID prescriptionId);
}
