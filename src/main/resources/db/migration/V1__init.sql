-- Flyway V1: Initial schema for Social Insurance Contributions API

-- Citizens table
CREATE TABLE citizens (
    id           UUID PRIMARY KEY,
    personal_code VARCHAR(50) NOT NULL UNIQUE,
    first_name    VARCHAR(100) NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    date_of_birth DATE NOT NULL,
    created_at    TIMESTAMP NOT NULL,
    updated_at    TIMESTAMP NOT NULL
);

-- Employers table
CREATE TABLE employers (
    id           UUID PRIMARY KEY,
    company_code VARCHAR(50) NOT NULL UNIQUE,
    name         VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP NOT NULL,
    updated_at   TIMESTAMP NOT NULL
);

-- Contributions table
CREATE TABLE contributions (
    id          UUID PRIMARY KEY,
    citizen_id  UUID NOT NULL,
    employer_id UUID NOT NULL,
    month_date  DATE NOT NULL,
    amount      NUMERIC(12, 2) NOT NULL,
    currency    CHAR(3) NOT NULL,
    paid_at     TIMESTAMP,
    created_at  TIMESTAMP NOT NULL,
    updated_at  TIMESTAMP NOT NULL,

    CONSTRAINT fk_contributions_citizen
        FOREIGN KEY (citizen_id) REFERENCES citizens (id),

    CONSTRAINT fk_contributions_employer
        FOREIGN KEY (employer_id) REFERENCES employers (id),

    CONSTRAINT uq_contributions_citizen_employer_month
        UNIQUE (citizen_id, employer_id, month_date)
);

-- Benefit claims table
CREATE TABLE benefit_claims (
    id              UUID PRIMARY KEY,
    citizen_id      UUID NOT NULL,
    type            VARCHAR(100) NOT NULL,
    status          VARCHAR(50) NOT NULL,
    start_date      DATE,
    end_date        DATE,
    submitted_at    TIMESTAMP NOT NULL,
    decided_at      TIMESTAMP,
    decision_reason VARCHAR(1000),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL,

    CONSTRAINT fk_benefit_claims_citizen
        FOREIGN KEY (citizen_id) REFERENCES citizens (id)
);

-- Indexes for foreign keys and month_date
CREATE INDEX idx_contributions_citizen_id ON contributions (citizen_id);
CREATE INDEX idx_contributions_employer_id ON contributions (employer_id);
CREATE INDEX idx_contributions_month_date ON contributions (month_date);

CREATE INDEX idx_benefit_claims_citizen_id ON benefit_claims (citizen_id);

