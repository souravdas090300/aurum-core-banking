-- ============================================================================
-- V3__sample_data.sql
-- Sample data matching actual database schema
-- ============================================================================

-- Sample Customers (with required fields from actual schema)
INSERT INTO customers (id, first_name, last_name, email, national_id_enc, date_of_birth, address_line1, city, country_code, is_pep, kyc_status, created_at, updated_at) VALUES
('11111111-1111-1111-1111-111111111111'::uuid, 'John', 'Doe', 'john.doe@example.com', 'ENC123456', '1985-03-15', '123 Main St', 'New York', 'US', false, 'VERIFIED', now(), now()),
('22222222-2222-2222-2222-222222222222'::uuid, 'Jane', 'Smith', 'jane.smith@example.com', 'ENC234567', '1990-07-22', '456 Oak Ave', 'Los Angeles', 'US', false, 'VERIFIED', now(), now()),
('33333333-3333-3333-3333-333333333333'::uuid, 'Bob', 'Johnson', 'bob.j@example.com', 'ENC345678', '1978-11-30', '789 Pine Rd', 'Chicago', 'US', false, 'VERIFIED', now(), now()),
('44444444-4444-4444-4444-444444444444'::uuid, 'Alice', 'Williams', 'alice.w@example.com', 'ENC456789', '1995-05-18', '321 Elm St', 'Houston', 'US', false, 'VERIFIED', now(), now()),
('55555555-5555-5555-5555-555555555555'::uuid, 'Corp', 'Business', 'contact@techcorp.com', 'ENC567890', '1980-01-01', '555 Business Blvd', 'San Francisco', 'US', false, 'VERIFIED', now(), now());

-- Sample Accounts (using CURRENT/SAVINGS/LOAN account types only)
INSERT INTO accounts (id, account_number, account_type, balance, currency, customer_id, status, created_at, updated_at) VALUES
('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'ACC001', 'SAVINGS', 4500.00, 'EUR', '11111111-1111-1111-1111-111111111111'::uuid, 'ACTIVE', now(), now()),
('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 'ACC002', 'CURRENT', 2800.00, 'EUR', '22222222-2222-2222-2222-222222222222'::uuid, 'ACTIVE', now(), now()),
('cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, 'ACC003', 'SAVINGS', 9700.00, 'EUR', '33333333-3333-3333-3333-333333333333'::uuid, 'ACTIVE', now(), now()),
('dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid, 'ACC004', 'CURRENT', 1800.00, 'EUR', '44444444-4444-4444-4444-444444444444'::uuid, 'ACTIVE', now(), now()),
('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'::uuid, 'ACC005', 'LOAN', 25000.00, 'EUR', '55555555-5555-5555-5555-555555555555'::uuid, 'ACTIVE', now(), now());

-- Sample Transactions (with required idempotency_key)
INSERT INTO transactions (id, idempotency_key, transaction_type, amount, currency, from_account_id, to_account_id, reference, status, executed_at, created_at) VALUES
('11111111-2222-3333-4444-555555555501'::uuid, 'KEY-001', 'DEPOSIT', 5000.00, 'EUR', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'Initial deposit', 'COMPLETED', now() - INTERVAL '30 days', now() - INTERVAL '30 days'),
('11111111-2222-3333-4444-555555555502'::uuid, 'KEY-002', 'TRANSFER', 500.00, 'EUR', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 'Payment to friend', 'COMPLETED', now() - INTERVAL '15 days', now() - INTERVAL '15 days'),
('11111111-2222-3333-4444-555555555503'::uuid, 'KEY-003', 'DEPOSIT', 2300.00, 'EUR', NULL, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 'Salary deposit', 'COMPLETED', now() - INTERVAL '29 days', now() - INTERVAL '29 days'),
('11111111-2222-3333-4444-555555555504'::uuid, 'KEY-004', 'WITHDRAWAL', 200.00, 'EUR', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, NULL, 'ATM withdrawal', 'COMPLETED', now() - INTERVAL '10 days', now() - INTERVAL '10 days'),
('11111111-2222-3333-4444-555555555505'::uuid, 'KEY-005', 'DEPOSIT', 10000.00, 'EUR', NULL, 'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, 'Investment deposit', 'COMPLETED', now() - INTERVAL '25 days', now() - INTERVAL '25 days'),
('11111111-2222-3333-4444-555555555506'::uuid, 'KEY-006', 'TRANSFER', 300.00, 'EUR', 'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, 'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid, 'Rent payment', 'COMPLETED', now() - INTERVAL '5 days', now() - INTERVAL '5 days'),
('11111111-2222-3333-4444-555555555507'::uuid, 'KEY-007', 'DEPOSIT', 1800.00, 'EUR', NULL, 'dddddddd-dddd-dddd-dddd-dddddddddddd'::uuid, 'Paycheck', 'COMPLETED', now() - INTERVAL '20 days', now() - INTERVAL '20 days'),
('11111111-2222-3333-4444-555555555508'::uuid, 'KEY-008', 'DEPOSIT', 25000.00, 'EUR', NULL, 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'::uuid, 'Business capital', 'COMPLETED', now() - INTERVAL '20 days', now() - INTERVAL '20 days');

-- Sample Loan Applications (using actual column names)
INSERT INTO loan_applications (id, customer_id, account_id, requested_amount, currency, term_months, purpose, credit_score, decision, submitted_at, created_at) VALUES
('11111111-3333-4444-5555-666666666601'::uuid, '11111111-1111-1111-1111-111111111111'::uuid, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa'::uuid, 50000.00, 'EUR', 60, 'Home improvement', 720, 'APPROVED', now() - INTERVAL '10 days', now() - INTERVAL '10 days'),
('11111111-3333-4444-5555-666666666602'::uuid, '22222222-2222-2222-2222-222222222222'::uuid, 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'::uuid, 150000.00, 'EUR', 360, 'Purchase property', 680, 'PENDING', now() - INTERVAL '5 days', now() - INTERVAL '5 days'),
('11111111-3333-4444-5555-666666666603'::uuid, '33333333-3333-3333-3333-333333333333'::uuid, 'cccccccc-cccc-cccc-cccc-cccccccccccc'::uuid, 25000.00, 'EUR', 48, 'Car purchase', 700, 'APPROVED', now() - INTERVAL '15 days', now() - INTERVAL '15 days'),
('11111111-3333-4444-5555-666666666604'::uuid, '55555555-5555-5555-5555-555555555555'::uuid, 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee'::uuid, 200000.00, 'EUR', 120, 'Business expansion', 750, 'PENDING', now() - INTERVAL '3 days', now() - INTERVAL '3 days');

COMMIT;
