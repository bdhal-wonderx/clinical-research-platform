package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.PatientVisit;
import com.wonderx.rwe.enums.VisitType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PatientVisitRepository extends JpaRepository<PatientVisit, UUID> {

    List<PatientVisit> findByPatientId(UUID patientId);

    List<PatientVisit> findByPatientIdAndVisitType(UUID patientId, VisitType visitType);
}
