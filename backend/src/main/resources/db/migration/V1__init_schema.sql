CREATE TABLE payments (
    id VARCHAR(255) PRIMARY KEY,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(50) NOT NULL,
    method VARCHAR(50),
    customer_id VARCHAR(255),
    contact VARCHAR(50),
    error_code VARCHAR(255),
    error_description TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE recovery_cases (
    id UUID PRIMARY KEY,
    payment_id VARCHAR(255) NOT NULL REFERENCES payments(id),
    status VARCHAR(50) NOT NULL,
    diagnosis VARCHAR(255),
    chosen_action VARCHAR(255),
    expected_value DECIMAL(19, 2),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    next_run_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE audit_events (
    id UUID PRIMARY KEY,
    case_id UUID NOT NULL REFERENCES recovery_cases(id),
    event_type VARCHAR(255) NOT NULL,
    payload JSONB NOT NULL,
    sequence_no INTEGER NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE recovery_policies (
    id UUID PRIMARY KEY,
    version INTEGER NOT NULL,
    config JSONB NOT NULL,
    last_changed_by VARCHAR(255) NOT NULL,
    last_changed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE evaluation_runs (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    started_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE evaluation_results (
    id UUID PRIMARY KEY,
    run_id UUID NOT NULL REFERENCES evaluation_runs(id),
    case_id UUID NOT NULL REFERENCES recovery_cases(id),
    baseline_arm VARCHAR(50) NOT NULL,
    recovered_amount DECIMAL(19, 2),
    policy_violations INTEGER NOT NULL DEFAULT 0,
    interventions INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
