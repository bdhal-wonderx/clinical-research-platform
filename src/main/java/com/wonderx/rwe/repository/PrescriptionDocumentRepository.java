package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.PrescriptionDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PrescriptionDocumentRepository extends JpaRepository<PrescriptionDocument, UUID> {

    List<PrescriptionDocument> findByPrescriptionIdOrderByVersionNoDesc(UUID prescriptionId);
}
