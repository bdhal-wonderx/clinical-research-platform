package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.DoctorDocument;
import com.wonderx.rwe.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DoctorDocumentRepository extends JpaRepository<DoctorDocument, UUID> {

    List<DoctorDocument> findByDoctorId(UUID doctorId);

    List<DoctorDocument> findByDoctorIdAndStudyId(UUID doctorId, UUID studyId);

    boolean existsByDoctorIdAndDocumentTypeAndStatus(UUID doctorId, DocumentType documentType, String status);
}
