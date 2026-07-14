package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.PatientConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PatientConsentRepository extends JpaRepository<PatientConsent, UUID> {

    Optional<PatientConsent> findByPatientId(UUID patientId);
}
