-- TOLERATE-HF: Full clinical pipeline schema

ALTER TABLE doctor_study ADD COLUMN IF NOT EXISTS patient_allocation INT NOT NULL DEFAULT 20;
ALTER TABLE doctor_study ADD COLUMN IF NOT EXISTS patients_enrolled INT NOT NULL DEFAULT 0;

ALTER TABLE study ADD COLUMN IF NOT EXISTS target_patients INT;
ALTER TABLE study ADD COLUMN IF NOT EXISTS target_sites INT;

-- ============================================================
-- STUDY CONFIGURATION EXTENSIONS
-- ============================================================

CREATE TABLE study_payment_rule (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    study_id        UUID         NOT NULL REFERENCES study(id),
    payment_type    VARCHAR(50)  NOT NULL DEFAULT 'PER_COMPLETED_PATIENT',
    amount          DECIMAL(12,2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'INR',
    requires_mou    BOOLEAN      NOT NULL DEFAULT TRUE,
    requires_consent BOOLEAN     NOT NULL DEFAULT TRUE,
    requires_both_visits BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE study_reminder_rule (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    study_id        UUID         NOT NULL REFERENCES study(id),
    followup_days   INT          NOT NULL DEFAULT 90,
    window_start_days INT        NOT NULL DEFAULT 80,
    window_end_days INT          NOT NULL DEFAULT 100,
    sms_days_before INT          NOT NULL DEFAULT 10,
    voice_escalation_hours INT   NOT NULL DEFAULT 48,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INVESTIGATOR EXTENSIONS
-- ============================================================

CREATE TABLE doctor_payment_profile (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL UNIQUE REFERENCES doctor(id),
    bank_account    VARCHAR(50),
    ifsc_code       VARCHAR(20),
    bank_name       VARCHAR(100),
    upi_id          VARCHAR(100),
    account_holder  VARCHAR(150),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE doctor_document (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    study_id        UUID         REFERENCES study(id),
    document_type   VARCHAR(50)  NOT NULL,
    document_url    VARCHAR(500),
    esign_ref       VARCHAR(100),
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    signed_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(doctor_id, study_id, document_type)
);

-- ============================================================
-- PATIENT REGISTRATION
-- ============================================================

CREATE TABLE patient (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    study_id        UUID         NOT NULL REFERENCES study(id),
    patient_token   VARCHAR(50)  NOT NULL UNIQUE,
    age             INT,
    gender          VARCHAR(20),
    city            VARCHAR(100),
    mobile_number   VARCHAR(15),
    smoking         BOOLEAN,
    alcohol         BOOLEAN,
    gdmt_phenotype  VARCHAR(30),
    status          VARCHAR(30)  NOT NULL DEFAULT 'REGISTERED',
    consent_captured BOOLEAN     NOT NULL DEFAULT FALSE,
    baseline_completed BOOLEAN   NOT NULL DEFAULT FALSE,
    followup_completed BOOLEAN    NOT NULL DEFAULT FALSE,
    enrolled_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE patient_consent (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id      UUID         NOT NULL UNIQUE REFERENCES patient(id),
    consent_type    VARCHAR(50)  NOT NULL DEFAULT 'INFORMED_CONSENT',
    consent_image_url VARCHAR(500) NOT NULL,
    captured_by     UUID         REFERENCES doctor(id),
    captured_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE patient_visit (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id      UUID         NOT NULL REFERENCES patient(id),
    visit_type      VARCHAR(20)  NOT NULL,
    visit_number    INT          NOT NULL,
    visit_date      DATE,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(patient_id, visit_number)
);

-- ============================================================
-- PRESCRIPTION & OCR
-- ============================================================

CREATE TABLE prescription (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_visit_id UUID        NOT NULL UNIQUE REFERENCES patient_visit(id),
    patient_id      UUID         NOT NULL REFERENCES patient(id),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    study_id        UUID         NOT NULL REFERENCES study(id),
    status          VARCHAR(30)  NOT NULL DEFAULT 'UPLOADED',
    locked          BOOLEAN      NOT NULL DEFAULT FALSE,
    locked_at       TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE prescription_document (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID         NOT NULL REFERENCES prescription(id),
    document_url    VARCHAR(500) NOT NULL,
    file_name       VARCHAR(255),
    mime_type       VARCHAR(100),
    version_no      INT          NOT NULL DEFAULT 1,
    superseded      BOOLEAN      NOT NULL DEFAULT FALSE,
    uploaded_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE ocr_result (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID         NOT NULL UNIQUE REFERENCES prescription(id),
    ocr_response    JSONB        NOT NULL DEFAULT '{}',
    extracted_data  JSONB        NOT NULL DEFAULT '{}',
    confidence_score DECIMAL(5,2),
    status          VARCHAR(30)  NOT NULL DEFAULT 'OCR_COMPLETED',
    processed_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE qc_review (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID         NOT NULL UNIQUE REFERENCES prescription(id),
    ocr_result_id   UUID         NOT NULL REFERENCES ocr_result(id),
    qc_status       VARCHAR(30)  NOT NULL DEFAULT 'QC_PENDING',
    reviewed_data   JSONB,
    changes         JSONB,
    approved_by     VARCHAR(100),
    approved_at     TIMESTAMPTZ,
    reviewer_notes  TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE validation_result (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID         NOT NULL REFERENCES prescription(id),
    patient_id      UUID         NOT NULL REFERENCES patient(id),
    overall_status  VARCHAR(20)  NOT NULL,
    rule_results    JSONB        NOT NULL DEFAULT '[]',
    validated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE patient_ecrf (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID         NOT NULL UNIQUE REFERENCES prescription(id),
    patient_id      UUID         NOT NULL REFERENCES patient(id),
    ecrf_data       JSONB        NOT NULL DEFAULT '{}',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- FOLLOW-UP & NOTIFICATIONS
-- ============================================================

CREATE TABLE followup_schedule (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    patient_id      UUID         NOT NULL UNIQUE REFERENCES patient(id),
    study_id        UUID         NOT NULL REFERENCES study(id),
    baseline_date   DATE         NOT NULL,
    due_date        DATE         NOT NULL,
    window_start    DATE         NOT NULL,
    window_end      DATE         NOT NULL,
    status          VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    sms_sent        BOOLEAN      NOT NULL DEFAULT FALSE,
    voice_sent      BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE followup_history (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    followup_schedule_id UUID    NOT NULL REFERENCES followup_schedule(id),
    patient_id      UUID         NOT NULL REFERENCES patient(id),
    completed_at    TIMESTAMPTZ,
    outcome         VARCHAR(50),
    notes           TEXT,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE notification_log (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_type  VARCHAR(30)  NOT NULL,
    recipient_id    UUID         NOT NULL,
    followup_schedule_id UUID,
    channel         VARCHAR(20)  NOT NULL,
    template_code   VARCHAR(50),
    message_body    TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- PAYMENTS & QUERIES
-- ============================================================

CREATE TABLE payment (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    study_id        UUID         NOT NULL REFERENCES study(id),
    patient_id      UUID         NOT NULL REFERENCES patient(id),
    amount          DECIMAL(12,2) NOT NULL,
    currency        VARCHAR(3)   NOT NULL DEFAULT 'INR',
    status          VARCHAR(30)  NOT NULL DEFAULT 'BLOCKED',
    block_reason    VARCHAR(255),
    paid_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(patient_id)
);

CREATE TABLE support_query (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    category        VARCHAR(50)  NOT NULL,
    subject         VARCHAR(255) NOT NULL,
    detail          TEXT,
    status          VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    resolved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE data_query (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prescription_id UUID         NOT NULL REFERENCES prescription(id),
    raised_by       VARCHAR(100) NOT NULL,
    reason          TEXT         NOT NULL,
    requested_changes JSONB,
    status          VARCHAR(30)  NOT NULL DEFAULT 'OPEN',
    resolved_at     TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_patient_doctor ON patient(doctor_id);
CREATE INDEX idx_patient_study ON patient(study_id);
CREATE INDEX idx_patient_token ON patient(patient_token);
CREATE INDEX idx_patient_status ON patient(status);
CREATE INDEX idx_prescription_patient ON prescription(patient_id);
CREATE INDEX idx_prescription_status ON prescription(status);
CREATE INDEX idx_followup_due ON followup_schedule(due_date, status);
CREATE INDEX idx_payment_doctor ON payment(doctor_id, status);
CREATE INDEX idx_notification_status ON notification_log(status, channel);
