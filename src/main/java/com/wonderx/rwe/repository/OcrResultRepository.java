package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OcrResultRepository extends JpaRepository<OcrResult, UUID> {

    Optional<OcrResult> findByPrescriptionId(UUID prescriptionId);
}
