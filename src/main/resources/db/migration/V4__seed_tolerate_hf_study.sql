-- TOLERATE-HF study configuration seed

UPDATE study SET
    study_code = 'TOLERATE-HF',
    study_name = 'TOLERATE-HF - GDMT in HFrEF Real World Study',
    description = 'Therapy Optimization & Limitations Evaluated in Real-world Analysis of Treatment-resistant Heart Failure',
    target_patients = 3000,
    target_sites = 150
WHERE id = 'a0000000-0000-0000-0000-000000000001';

UPDATE study_protocol SET
    protocol_name = 'TOLERATE-HF Protocol v1.0'
WHERE id = 'b0000000-0000-0000-0000-000000000001';

-- TOLERATE-HF validation rules
INSERT INTO study_protocol_rule (protocol_id, rule_name, rule_type, field_name, operator, expected_value, severity, display_order)
VALUES
    ('b0000000-0000-0000-0000-000000000001', 'HFrEF EF Threshold', 'CLINICAL', 'ef', 'LTE', '45', 'FAIL', 5),
    ('b0000000-0000-0000-0000-000000000001', 'NYHA Required', 'CLINICAL', 'nyha', 'NOT_NULL', NULL, 'FAIL', 6),
    ('b0000000-0000-0000-0000-000000000001', 'GDMT Phenotype Required', 'CLINICAL', 'gdmtPhenotype', 'NOT_NULL', NULL, 'FAIL', 7),
    ('b0000000-0000-0000-0000-000000000001', 'NT-proBNP Warning', 'CLINICAL', 'ntProBnp', 'NOT_NULL', NULL, 'WARNING', 8);

INSERT INTO study_payment_rule (study_id, payment_type, amount, currency)
VALUES ('a0000000-0000-0000-0000-000000000001', 'PER_COMPLETED_PATIENT', 5000.00, 'INR');

INSERT INTO study_reminder_rule (study_id, followup_days, window_start_days, window_end_days, sms_days_before, voice_escalation_hours)
VALUES ('a0000000-0000-0000-0000-000000000001', 90, 80, 100, 10, 48);
