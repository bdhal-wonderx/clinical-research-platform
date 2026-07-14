package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.StudyPaymentRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyPaymentRuleRepository extends JpaRepository<StudyPaymentRule, UUID> {

    List<StudyPaymentRule> findByStudyId(UUID studyId);
}
