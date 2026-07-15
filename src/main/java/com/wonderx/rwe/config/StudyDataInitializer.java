package com.wonderx.rwe.config;

import com.wonderx.rwe.entity.Study;
import com.wonderx.rwe.entity.StudyPaymentRule;
import com.wonderx.rwe.entity.StudyProtocol;
import com.wonderx.rwe.entity.StudyProtocolRule;
import com.wonderx.rwe.entity.StudyReminderRule;
import com.wonderx.rwe.enums.StudyStatus;
import com.wonderx.rwe.repository.StudyPaymentRuleRepository;
import com.wonderx.rwe.repository.StudyProtocolRepository;
import com.wonderx.rwe.repository.StudyProtocolRuleRepository;
import com.wonderx.rwe.repository.StudyReminderRuleRepository;
import com.wonderx.rwe.repository.StudyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StudyDataInitializer implements CommandLineRunner {

    public static final UUID TOLERATE_HF_STUDY_ID = UUID.fromString("a0000000-0000-0000-0000-000000000001");
    public static final UUID TOLERATE_HF_PROTOCOL_ID = UUID.fromString("b0000000-0000-0000-0000-000000000001");

    private final StudyRepository studyRepository;
    private final StudyProtocolRepository protocolRepository;
    private final StudyProtocolRuleRepository ruleRepository;
    private final StudyPaymentRuleRepository paymentRuleRepository;
    private final StudyReminderRuleRepository reminderRuleRepository;

    @Override
    public void run(String... args) {
        if (studyRepository.existsById(TOLERATE_HF_STUDY_ID)) {
            return;
        }

        log.info("Seeding TOLERATE-HF study data");

        Study study = Study.builder()
                .id(TOLERATE_HF_STUDY_ID)
                .studyCode("TOLERATE-HF")
                .studyName("TOLERATE-HF - GDMT in HFrEF Real World Study")
                .description("Therapy Optimization & Limitations Evaluated in Real-world Analysis of Treatment-resistant Heart Failure")
                .status(StudyStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2027, 12, 31))
                .targetPatients(3000)
                .targetSites(150)
                .build();
        studyRepository.save(study);

        StudyProtocol protocol = StudyProtocol.builder()
                .id(TOLERATE_HF_PROTOCOL_ID)
                .study(study)
                .protocolVersion("v1.0")
                .protocolName("TOLERATE-HF Protocol v1.0")
                .effectiveFrom(LocalDate.of(2026, 1, 1))
                .status("ACTIVE")
                .build();
        protocolRepository.save(protocol);

        ruleRepository.save(StudyProtocolRule.builder().protocol(protocol).ruleName("Minimum Age")
                .ruleType("ELIGIBILITY").fieldName("age").operator("GTE").expectedValue("18")
                .severity("FAIL").displayOrder(1).build());
        ruleRepository.save(StudyProtocolRule.builder().protocol(protocol).ruleName("Maximum Age")
                .ruleType("ELIGIBILITY").fieldName("age").operator("LTE").expectedValue("85")
                .severity("FAIL").displayOrder(2).build());
        ruleRepository.save(StudyProtocolRule.builder().protocol(protocol).ruleName("Consent Required")
                .ruleType("COMPLIANCE").fieldName("consent_captured").operator("EQ").expectedValue("true")
                .severity("FAIL").displayOrder(3).build());
        ruleRepository.save(StudyProtocolRule.builder().protocol(protocol).ruleName("HFrEF EF Threshold")
                .ruleType("CLINICAL").fieldName("ef").operator("LTE").expectedValue("45")
                .severity("FAIL").displayOrder(5).build());
        ruleRepository.save(StudyProtocolRule.builder().protocol(protocol).ruleName("NYHA Required")
                .ruleType("CLINICAL").fieldName("nyha").operator("NOT_NULL").expectedValue(null)
                .severity("FAIL").displayOrder(6).build());
        ruleRepository.save(StudyProtocolRule.builder().protocol(protocol).ruleName("GDMT Phenotype Required")
                .ruleType("CLINICAL").fieldName("gdmtPhenotype").operator("NOT_NULL").expectedValue(null)
                .severity("FAIL").displayOrder(7).build());

        paymentRuleRepository.save(StudyPaymentRule.builder()
                .study(study).paymentType("PER_COMPLETED_PATIENT")
                .amount(new BigDecimal("5000.00")).currency("INR").build());

        reminderRuleRepository.save(StudyReminderRule.builder()
                .study(study).followupDays(90).windowStartDays(80).windowEndDays(100)
                .smsDaysBefore(10).voiceEscalationHours(48).build());
    }
}
