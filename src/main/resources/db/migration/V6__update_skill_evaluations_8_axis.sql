-- V6: Update skill_evaluations table to use 8-axis performance metrics
-- Migration from 7-axis (footwork, strokes, stamina, attack, defence, agility, court_coverage)
-- to 8-axis (smash_power, net_control, backhand, footwork, agility, stamina, tactical_awareness, mental_strength)

-- Add new columns
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS smash_power INTEGER;
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS net_control INTEGER;
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS backhand INTEGER;
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS tactical_awareness INTEGER;
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS mental_strength INTEGER;
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS month INTEGER;
ALTER TABLE skill_evaluations ADD COLUMN IF NOT EXISTS year INTEGER;

-- Migrate data: Map old columns to new columns with reasonable defaults
-- strokes -> smash_power (similar concept)
-- attack -> net_control (offensive play)
-- defence -> backhand (defensive stroke)
-- court_coverage -> tactical_awareness (court awareness)
UPDATE skill_evaluations 
SET 
    smash_power = COALESCE(strokes, 5),
    net_control = COALESCE(attack, 5),
    backhand = COALESCE(defence, 5),
    tactical_awareness = COALESCE(court_coverage, 5),
    mental_strength = 5,  -- New field, default to middle value
    month = EXTRACT(MONTH FROM evaluated_at),
    year = EXTRACT(YEAR FROM evaluated_at)
WHERE smash_power IS NULL;

-- Make new columns NOT NULL after migration
ALTER TABLE skill_evaluations ALTER COLUMN smash_power SET NOT NULL;
ALTER TABLE skill_evaluations ALTER COLUMN net_control SET NOT NULL;
ALTER TABLE skill_evaluations ALTER COLUMN backhand SET NOT NULL;
ALTER TABLE skill_evaluations ALTER COLUMN tactical_awareness SET NOT NULL;
ALTER TABLE skill_evaluations ALTER COLUMN mental_strength SET NOT NULL;

-- Drop old columns that are no longer needed
ALTER TABLE skill_evaluations DROP COLUMN IF EXISTS strokes;
ALTER TABLE skill_evaluations DROP COLUMN IF EXISTS attack;
ALTER TABLE skill_evaluations DROP COLUMN IF EXISTS defence;
ALTER TABLE skill_evaluations DROP COLUMN IF EXISTS court_coverage;
