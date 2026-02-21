-- Migration: Add makeup attendance support and audit trail
-- This migration adds:
-- 1. entry_type column to attendance table (REGULAR/MAKEUP)
-- 2. compensates_for_date column to link makeup to missed session
-- 3. was_backdated and backdate_reason columns for backdated entries
-- 4. attendance_audit_log table for complete audit trail

-- ==================== ATTENDANCE TABLE UPDATES ====================

-- Add entry_type column (REGULAR or MAKEUP)
ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS entry_type VARCHAR(20) DEFAULT 'REGULAR';

-- Add compensates_for_date for linking makeup to original absence
ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS compensates_for_date DATE;

-- Add was_backdated flag
ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS was_backdated BOOLEAN DEFAULT FALSE;

-- Add backdate_reason for audit purposes
ALTER TABLE attendance 
ADD COLUMN IF NOT EXISTS backdate_reason VARCHAR(500);

-- Add index for compensates_for_date lookups
CREATE INDEX IF NOT EXISTS idx_attendance_compensates_for_date 
ON attendance(compensates_for_date) WHERE compensates_for_date IS NOT NULL;

-- Add index for backdated queries
CREATE INDEX IF NOT EXISTS idx_attendance_was_backdated 
ON attendance(was_backdated) WHERE was_backdated = TRUE;

-- ==================== ATTENDANCE AUDIT LOG TABLE ====================

CREATE TABLE IF NOT EXISTS attendance_audit_log (
    id BIGSERIAL PRIMARY KEY,
    attendance_id BIGINT NOT NULL,
    action VARCHAR(20) NOT NULL, -- CREATE, UPDATE, DELETE
    
    -- Previous values (NULL for CREATE)
    previous_status VARCHAR(20),
    previous_entry_type VARCHAR(20),
    previous_notes VARCHAR(500),
    
    -- New values (NULL for DELETE)
    new_status VARCHAR(20),
    new_entry_type VARCHAR(20),
    new_notes VARCHAR(500),
    
    -- Who made the change (references coaches.user_id)
    changed_by_id BIGINT,
    changed_by_role VARCHAR(20) NOT NULL, -- COACH, ADMIN
    
    -- Additional audit info
    reason VARCHAR(500),
    was_backdated BOOLEAN DEFAULT FALSE,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_audit_attendance FOREIGN KEY (attendance_id) REFERENCES attendance(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_changed_by FOREIGN KEY (changed_by_id) REFERENCES coaches(user_id)
);

-- Add indexes for common audit queries
CREATE INDEX IF NOT EXISTS idx_audit_attendance_id ON attendance_audit_log(attendance_id);
CREATE INDEX IF NOT EXISTS idx_audit_changed_by_id ON attendance_audit_log(changed_by_id);
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON attendance_audit_log(changed_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_was_backdated ON attendance_audit_log(was_backdated) WHERE was_backdated = TRUE;

-- ==================== UPDATE EXISTING DATA ====================

-- Set all existing attendance records to REGULAR entry type
UPDATE attendance SET entry_type = 'REGULAR' WHERE entry_type IS NULL;

-- Set was_backdated to false for all existing records
UPDATE attendance SET was_backdated = FALSE WHERE was_backdated IS NULL;

COMMENT ON COLUMN attendance.entry_type IS 'Type of attendance entry: REGULAR for scheduled sessions, MAKEUP for makeup sessions';
COMMENT ON COLUMN attendance.compensates_for_date IS 'For MAKEUP entries, the date of the original missed session this makeup compensates for';
COMMENT ON COLUMN attendance.was_backdated IS 'True if this attendance was marked or modified for a past date';
COMMENT ON COLUMN attendance.backdate_reason IS 'Required explanation when marking attendance for past dates';
COMMENT ON TABLE attendance_audit_log IS 'Complete audit trail for all attendance changes including backdated modifications';
