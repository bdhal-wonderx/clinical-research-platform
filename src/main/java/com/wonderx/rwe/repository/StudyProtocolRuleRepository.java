package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.StudyProtocolRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudyProtocolRuleRepository extends JpaRepository<StudyProtocolRule, UUID> {

    List<StudyProtocolRule> findByProtocolIdAndIsActiveTrueOrderByDisplayOrder(UUID protocolId);
}
