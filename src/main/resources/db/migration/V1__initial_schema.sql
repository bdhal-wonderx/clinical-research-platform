-- RWE Platform - Doctor Onboarding Schema

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- STUDY CONFIGURATION (required for doctor study assignment)
-- ============================================================

CREATE TABLE study (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    study_code      VARCHAR(50)  NOT NULL UNIQUE,
    study_name      VARCHAR(255) NOT NULL,
    description     TEXT,
    status          VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    start_date      DATE,
    end_date        DATE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE study_protocol (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    study_id        UUID         NOT NULL REFERENCES study(id),
    protocol_version VARCHAR(20) NOT NULL,
    protocol_name   VARCHAR(255) NOT NULL,
    effective_from  DATE         NOT NULL,
    effective_to    DATE,
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(study_id, protocol_version)
);

CREATE TABLE study_protocol_rule (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    protocol_id     UUID         NOT NULL REFERENCES study_protocol(id),
    rule_name       VARCHAR(100) NOT NULL,
    rule_type       VARCHAR(50)  NOT NULL,
    field_name      VARCHAR(100) NOT NULL,
    operator        VARCHAR(20)  NOT NULL,
    expected_value  TEXT,
    severity        VARCHAR(20)  NOT NULL DEFAULT 'FAIL',
    display_order   INT          NOT NULL DEFAULT 0,
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- DOCTOR ONBOARDING
-- ============================================================

CREATE TABLE doctor (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    mobile_number   VARCHAR(15)  NOT NULL UNIQUE,
    email           VARCHAR(255),
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING_OTP',
    otp_verified_at TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE doctor_profile (
    id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id                   UUID         NOT NULL UNIQUE REFERENCES doctor(id),
    medical_registration_number VARCHAR(100) NOT NULL,
    registration_council        VARCHAR(100) NOT NULL,
    registration_year           INT,
    hospital_name               VARCHAR(255) NOT NULL,
    hospital_address            TEXT,
    hospital_city               VARCHAR(100),
    hospital_state              VARCHAR(100),
    hospital_pincode            VARCHAR(10),
    specialization              VARCHAR(150) NOT NULL,
    years_of_experience         INT,
    created_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE doctor_mou (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    study_id        UUID         REFERENCES study(id),
    mou_document_url VARCHAR(500),
    mou_signed_at   TIMESTAMPTZ,
    mou_status      VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    digital_signature TEXT,
    terms_accepted  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE doctor_study (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    doctor_id       UUID         NOT NULL REFERENCES doctor(id),
    study_id        UUID         NOT NULL REFERENCES study(id),
    assigned_at     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    assigned_by     VARCHAR(100),
    status          VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    UNIQUE(doctor_id, study_id)
);

-- ============================================================
-- AUDIT
-- ============================================================

CREATE TABLE audit_log (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type     VARCHAR(50)  NOT NULL,
    entity_id       UUID         NOT NULL,
    action          VARCHAR(50)  NOT NULL,
    performed_by    VARCHAR(100),
    old_value       JSONB,
    new_value       JSONB,
    ip_address      VARCHAR(45),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_doctor_mobile ON doctor(mobile_number);
CREATE INDEX idx_doctor_status ON doctor(status);
CREATE INDEX idx_doctor_study_doctor ON doctor_study(doctor_id);
CREATE INDEX idx_doctor_study_study ON doctor_study(study_id);
CREATE INDEX idx_audit_log_entity ON audit_log(entity_type, entity_id);
