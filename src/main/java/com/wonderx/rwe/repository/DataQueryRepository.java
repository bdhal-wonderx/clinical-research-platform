package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.DataQuery;
import com.wonderx.rwe.enums.QueryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataQueryRepository extends JpaRepository<DataQuery, UUID> {

    List<DataQuery> findByPrescriptionId(UUID prescriptionId);

    List<DataQuery> findByStatus(QueryStatus status);
}
