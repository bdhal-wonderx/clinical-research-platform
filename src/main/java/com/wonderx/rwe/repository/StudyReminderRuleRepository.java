package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.StudyReminderRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyReminderRuleRepository extends JpaRepository<StudyReminderRule, UUID> {

    List<StudyReminderRule> findByStudyId(UUID studyId);
}
