-- Add email column to payments table (nullable — Razorpay only includes it when customer provides one)
ALTER TABLE payments ADD COLUMN IF NOT EXISTS email VARCHAR(255);
