package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.DoctorPaymentProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorPaymentProfileRepository extends JpaRepository<DoctorPaymentProfile, UUID> {

    Optional<DoctorPaymentProfile> findByDoctorId(UUID doctorId);
}
