-- ============================================================
-- Aurum Core Banking — Initial Schema
-- V1__init_schema.sql
-- ============================================================

-- CUSTOMERS table
CREATE TABLE customers (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100)    NOT NULL,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    national_id_enc     VARCHAR(512)    NOT NULL,   -- AES-256 encrypted
    date_of_birth       DATE            NOT NULL,
    address_line1       VARCHAR(255)    NOT NULL,
    address_line2       VARCHAR(255),
    city                VARCHAR(100)    NOT NULL,
    country_code        CHAR(2)         NOT NULL,   -- ISO 3166-1
    is_pep              BOOLEAN         NOT NULL DEFAULT FALSE,
    kyc_status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    kyc_verified_at     TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- ACCOUNTS table
CREATE TABLE accounts (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number      VARCHAR(34)     NOT NULL UNIQUE,  -- IBAN format
    customer_id         UUID            NOT NULL REFERENCES customers(id),
    account_type        VARCHAR(20)     NOT NULL,         -- CURRENT, SAVINGS, LOAN
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    balance             NUMERIC(19,4)   NOT NULL DEFAULT 0.0000,
    currency            CHAR(3)         NOT NULL DEFAULT 'EUR',
    credit_limit        NUMERIC(19,4)   NOT NULL DEFAULT 0.0000,
    opened_at           TIMESTAMP       NOT NULL DEFAULT NOW(),
    closed_at           TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_balance_not_negative CHECK (balance >= -credit_limit)
);

-- TRANSACTIONS table
CREATE TABLE transactions (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    idempotency_key     VARCHAR(64)     NOT NULL UNIQUE,  -- prevents duplicates
    from_account_id     UUID            REFERENCES accounts(id),
    to_account_id       UUID            REFERENCES accounts(id),
    amount              NUMERIC(19,4)   NOT NULL,
    currency            CHAR(3)         NOT NULL DEFAULT 'EUR',
    transaction_type    VARCHAR(30)     NOT NULL,
    status              VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    reference           VARCHAR(140),
    fraud_flag          VARCHAR(30),
    executed_at         TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    version             BIGINT          NOT NULL DEFAULT 0,
    CONSTRAINT chk_amount_positive CHECK (amount > 0)
);

-- AUDIT_LOG table (immutable — no updates or deletes ever)
CREATE TABLE audit_log (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    entity_type         VARCHAR(50)     NOT NULL,
    entity_id           UUID            NOT NULL,
    action              VARCHAR(50)     NOT NULL,
    performed_by        VARCHAR(255)    NOT NULL,
    old_value           JSONB,
    new_value           JSONB,
    ip_address          VARCHAR(45),
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

-- LOAN_APPLICATIONS table
CREATE TABLE loan_applications (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id         UUID            NOT NULL REFERENCES customers(id),
    account_id          UUID            REFERENCES accounts(id),
    requested_amount    NUMERIC(19,4)   NOT NULL,
    currency            CHAR(3)         NOT NULL DEFAULT 'EUR',
    term_months         INT             NOT NULL,
    purpose             VARCHAR(255),
    credit_score        INT,
    decision            VARCHAR(20),    -- APPROVED, DECLINED, REVIEW
    jbpm_process_id     VARCHAR(100),   -- jBPM workflow instance ID
    submitted_at        TIMESTAMP       NOT NULL DEFAULT NOW(),
    decided_at          TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW(),
    version             BIGINT          NOT NULL DEFAULT 0
);

-- ── INDEXES ──
CREATE INDEX idx_accounts_customer    ON accounts(customer_id);
CREATE INDEX idx_accounts_number      ON accounts(account_number);
CREATE INDEX idx_transactions_from    ON transactions(from_account_id);
CREATE INDEX idx_transactions_to      ON transactions(to_account_id);
CREATE INDEX idx_transactions_created ON transactions(created_at DESC);
CREATE INDEX idx_audit_entity         ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_created        ON audit_log(created_at DESC);
CREATE INDEX idx_loans_customer       ON loan_applications(customer_id);