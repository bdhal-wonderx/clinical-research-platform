package com.wonderx.rwe.service;

import com.wonderx.rwe.entity.*;
import com.wonderx.rwe.enums.PaymentStatus;
import com.wonderx.rwe.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StudyPaymentRuleRepository paymentRuleRepository;
    private final DoctorDocumentRepository documentRepository;
    private final DoctorMouRepository mouRepository;

    @Transactional
    public Payment evaluatePayment(Patient patient) {
        StudyPaymentRule rule = paymentRuleRepository.findByStudyId(patient.getStudy().getId())
                .stream().findFirst()
                .orElse(StudyPaymentRule.builder().amount(new BigDecimal("5000")).currency("INR").build());

        Payment payment = paymentRepository.findByPatientId(patient.getId())
                .orElse(Payment.builder()
                        .doctor(patient.getDoctor())
                        .study(patient.getStudy())
                        .patient(patient)
                        .amount(rule.getAmount())
                        .currency(rule.getCurrency())
                        .build());

        if (!Boolean.TRUE.equals(patient.getConsentCaptured())) {
            payment.setStatus(PaymentStatus.BLOCKED);
            payment.setBlockReason("Consent not captured");
        } else if (!Boolean.TRUE.equals(patient.getBaselineCompleted()) || !Boolean.TRUE.equals(patient.getFollowupCompleted())) {
            payment.setStatus(PaymentStatus.BLOCKED);
            payment.setBlockReason("Both visits not completed");
        } else if (!isMouSigned(patient.getDoctor().getId(), patient.getStudy().getId())) {
            payment.setStatus(PaymentStatus.BLOCKED);
            payment.setBlockReason("MOU not signed");
        } else {
            payment.setStatus(PaymentStatus.PAYABLE);
            payment.setBlockReason(null);
        }

        return paymentRepository.save(payment);
    }

    private boolean isMouSigned(java.util.UUID doctorId, java.util.UUID studyId) {
        return mouRepository.findByDoctorIdAndStudyId(doctorId, studyId)
                .map(m -> m.getMouStatus().name().equals("SIGNED"))
                .orElse(false);
    }

    @Transactional
    public Payment markPaid(java.util.UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new com.wonderx.rwe.exception.ResourceNotFoundException("Payment not found"));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(Instant.now());
        return paymentRepository.save(payment);
    }
}
