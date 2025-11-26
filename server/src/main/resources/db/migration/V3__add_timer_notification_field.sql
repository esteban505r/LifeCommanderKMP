-- Migration: Add send_notification_on_complete field to timers table
-- This field controls whether a push notification is sent when the timer completes.
-- Defaults to true for existing timers to maintain current behavior.

ALTER TABLE timers
ADD COLUMN IF NOT EXISTS send_notification_on_complete BOOLEAN DEFAULT true;

-- Update existing timers to have notifications enabled by default
UPDATE timers
SET send_notification_on_complete = true
WHERE send_notification_on_complete IS NULL;

