-- V5: Rename expected_value and add actual_recovered_amount, payment_link_url
ALTER TABLE recovery_cases RENAME COLUMN expected_value TO expected_recovery_value;
ALTER TABLE recovery_cases ADD COLUMN actual_recovered_amount NUMERIC(10,2);
ALTER TABLE recovery_cases ADD COLUMN payment_link_url VARCHAR(512);
