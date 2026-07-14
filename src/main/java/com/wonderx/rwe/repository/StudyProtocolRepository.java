package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.StudyProtocol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyProtocolRepository extends JpaRepository<StudyProtocol, UUID> {

    List<StudyProtocol> findByStudyIdAndStatus(UUID studyId, String status);
}
