CREATE OR REPLACE FUNCTION prevent_audit_modifications()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'audit_events is append-only. UPDATE and DELETE are not allowed.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER audit_events_append_only
BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW
EXECUTE FUNCTION prevent_audit_modifications();
