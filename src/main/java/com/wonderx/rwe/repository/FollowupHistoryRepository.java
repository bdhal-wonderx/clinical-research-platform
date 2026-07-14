package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.FollowupHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FollowupHistoryRepository extends JpaRepository<FollowupHistory, UUID> {

    List<FollowupHistory> findByPatientId(UUID patientId);
}
