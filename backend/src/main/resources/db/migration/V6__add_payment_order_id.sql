-- Add order_id to payments to support payment.captured correlation for SEND_EMAIL recovery path.
-- Razorpay payment.captured webhooks carry order_id; we match against the original failed payment's order_id
-- to identify which recovery case to mark RECOVERED.
ALTER TABLE payments ADD COLUMN order_id VARCHAR(255);

CREATE INDEX idx_payments_order_id ON payments (order_id);
