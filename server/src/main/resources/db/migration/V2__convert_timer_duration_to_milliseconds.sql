-- Migration: Convert timer duration from seconds to milliseconds
-- This migration converts existing timer durations from seconds to milliseconds
-- by multiplying by 1000. New timers will be stored in milliseconds.

UPDATE timers
SET duration = duration * 1000
WHERE duration < 1000000; -- Only convert if it looks like seconds (less than ~11 days in ms)

-- Note: This assumes existing durations are in seconds (reasonable values < 1,000,000 seconds = ~11 days)
-- If a duration is already >= 1,000,000, we assume it's already in milliseconds and leave it as-is

