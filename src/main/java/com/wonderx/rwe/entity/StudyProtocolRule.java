package com.wonderx.rwe.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "study_protocol_rule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudyProtocolRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", nullable = false)
    private StudyProtocol protocol;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "rule_type", nullable = false, length = 50)
    private String ruleType;

    @Column(name = "field_name", nullable = false, length = 100)
    private String fieldName;

    @Column(nullable = false, length = 20)
    private String operator;

    @Column(name = "expected_value", columnDefinition = "TEXT")
    private String expectedValue;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String severity = "FAIL";

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
