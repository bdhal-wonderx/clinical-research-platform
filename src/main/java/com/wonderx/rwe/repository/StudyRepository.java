package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.Study;
import com.wonderx.rwe.enums.StudyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyRepository extends JpaRepository<Study, UUID> {

    Optional<Study> findByStudyCode(String studyCode);

    List<Study> findByStatus(StudyStatus status);
}
