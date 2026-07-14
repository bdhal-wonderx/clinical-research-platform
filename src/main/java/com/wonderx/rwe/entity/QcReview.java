package com.wonderx.rwe.entity;

import com.wonderx.rwe.enums.QcStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "qc_review")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QcReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prescription_id", nullable = false, unique = true)
    private Prescription prescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ocr_result_id", nullable = false)
    private OcrResult ocrResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "qc_status", nullable = false, length = 30)
    @Builder.Default
    private QcStatus qcStatus = QcStatus.QC_PENDING;

    @Column(name = "reviewed_data", columnDefinition = "jsonb")
    private String reviewedData;

    @Column(columnDefinition = "jsonb")
    private String changes;

    @Column(name = "approved_by", length = 100)
    private String approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "reviewer_notes", columnDefinition = "TEXT")
    private String reviewerNotes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
