package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.FollowupSchedule;
import com.wonderx.rwe.enums.FollowupStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowupScheduleRepository extends JpaRepository<FollowupSchedule, UUID> {

    Optional<FollowupSchedule> findByPatientId(UUID patientId);

    List<FollowupSchedule> findByStatus(FollowupStatus status);

    List<FollowupSchedule> findByDueDateBeforeAndStatus(LocalDate dueDate, FollowupStatus status);
}
