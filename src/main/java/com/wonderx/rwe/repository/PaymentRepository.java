package com.wonderx.rwe.repository;

import com.wonderx.rwe.entity.Payment;
import com.wonderx.rwe.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    List<Payment> findByDoctorId(UUID doctorId);

    Optional<Payment> findByPatientId(UUID patientId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.doctor.id = :doctorId")
    BigDecimal sumAmountByDoctorId(@Param("doctorId") UUID doctorId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.doctor.id = :doctorId AND p.status = :status")
    BigDecimal sumAmountByDoctorIdAndStatus(@Param("doctorId") UUID doctorId, @Param("status") PaymentStatus status);
}
