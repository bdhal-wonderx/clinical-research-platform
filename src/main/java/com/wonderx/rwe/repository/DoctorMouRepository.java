package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.DoctorMou;
import com.wonderx.rwe.enums.MouStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorMouRepository extends JpaRepository<DoctorMou, UUID> {

    List<DoctorMou> findByDoctorId(UUID doctorId);

    Optional<DoctorMou> findByDoctorIdAndStudyId(UUID doctorId, UUID studyId);

    boolean existsByDoctorIdAndMouStatus(UUID doctorId, MouStatus mouStatus);
}
