-- V4: Create policies table with named rows (replaces the JSONB blob recovery_policies approach)
CREATE TABLE policies (
    id          VARCHAR(20) PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    value       VARCHAR(100) NOT NULL,
    unit        VARCHAR(50)  NOT NULL,
    enabled     BOOLEAN      NOT NULL DEFAULT TRUE,
    updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_by  VARCHAR(255) NOT NULL DEFAULT 'system'
);

-- Audit log for policy changes (per rules.md: policy changes must themselves be auditable)
CREATE TABLE policy_audit_log (
    id          UUID PRIMARY KEY,
    policy_id   VARCHAR(20) NOT NULL REFERENCES policies(id),
    old_value   VARCHAR(100),
    new_value   VARCHAR(100),
    old_enabled BOOLEAN,
    new_enabled BOOLEAN,
    changed_by  VARCHAR(255) NOT NULL,
    changed_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Seed initial policy rows
INSERT INTO policies (id, name, description, value, unit, enabled, updated_by) VALUES
    ('POL-01', 'Max Recovery Cost',
     'Limit the AI''s execution budget per case. Payments above this amount require escalation.',
     '25000', 'INR', TRUE, 'system'),
    ('POL-02', 'DND Hours Exclusion',
     'Block communication during regulatory Do Not Disturb hours (TRAI guidelines).',
     '21:00-08:00', 'TIME', TRUE, 'system'),
    ('POL-03', 'Mandate Window Expiry',
     'Prevent retry actions 24 hours before mandate expires.',
     '24', 'HOURS', TRUE, 'system'),
    ('POL-04', 'Escalation Threshold',
     'Require manual approval for recovery amounts exceeding this limit. Cases are routed to ESCALATED status.',
     '30000', 'INR', TRUE, 'system');
