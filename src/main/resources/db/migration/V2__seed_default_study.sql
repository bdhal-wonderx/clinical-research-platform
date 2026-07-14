INSERT INTO study (id, study_code, study_name, description, status, start_date, end_date)
VALUES
    ('a0000000-0000-0000-0000-000000000001', 'RWE-HF-2026', 'Heart Failure Real World Evidence Study',
     'Multi-center observational study for heart failure patients', 'ACTIVE', '2026-01-01', '2027-12-31');

INSERT INTO study_protocol (id, study_id, protocol_version, protocol_name, effective_from, status)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'a0000000-0000-0000-0000-000000000001',
     'v1.0', 'HF RWE Protocol v1.0', '2026-01-01', 'ACTIVE');

INSERT INTO study_protocol_rule (protocol_id, rule_name, rule_type, field_name, operator, expected_value, severity, display_order)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'Minimum Age', 'ELIGIBILITY', 'age', 'GTE', '18', 'FAIL', 1),
    ('b0000000-0000-0000-0000-000000000001', 'Maximum Age', 'ELIGIBILITY', 'age', 'LTE', '85', 'FAIL', 2),
    ('b0000000-0000-0000-0000-000000000001', 'Consent Required', 'COMPLIANCE', 'consent_captured', 'EQ', 'true', 'FAIL', 3),
    ('b0000000-0000-0000-0000-000000000001', 'LVEF Threshold', 'CLINICAL', 'lvef', 'LT', '40', 'WARNING', 4);
