package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.SupportQuery;
import com.wonderx.rwe.enums.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SupportQueryRepository extends JpaRepository<SupportQuery, UUID> {

    List<SupportQuery> findByDoctorId(UUID doctorId);

    List<SupportQuery> findByStatus(QueryStatus status);
}
