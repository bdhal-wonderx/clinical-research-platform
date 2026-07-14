package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.DoctorStudy;
import com.wonderx.rwe.enums.DoctorStudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorStudyRepository extends JpaRepository<DoctorStudy, UUID> {

    List<DoctorStudy> findByDoctorId(UUID doctorId);

    List<DoctorStudy> findByStudyId(UUID studyId);

    Optional<DoctorStudy> findByDoctorIdAndStudyId(UUID doctorId, UUID studyId);

    boolean existsByDoctorIdAndStudyIdAndStatus(UUID doctorId, UUID studyId, DoctorStudyStatus status);
}
