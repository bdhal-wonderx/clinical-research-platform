package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.QcReview;
import com.wonderx.rwe.enums.QcStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface QcReviewRepository extends JpaRepository<QcReview, UUID> {

    Optional<QcReview> findByPrescriptionId(UUID prescriptionId);

    List<QcReview> findByQcStatus(QcStatus qcStatus);
}
