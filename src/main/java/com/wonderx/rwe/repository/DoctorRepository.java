package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.Doctor;
import com.wonderx.rwe.enums.DoctorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, UUID> {

    Optional<Doctor> findByMobileNumber(String mobileNumber);

    boolean existsByMobileNumber(String mobileNumber);

    List<Doctor> findByStatus(DoctorStatus status);
}
