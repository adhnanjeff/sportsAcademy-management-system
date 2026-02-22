-- =====================================================
-- V8: Performance Optimization Indexes
-- =====================================================
-- This migration adds comprehensive indexes to improve
-- query performance and reduce N+1 query issues.
-- =====================================================

-- ===================
-- Users Table Indexes
-- ===================
-- Email lookups (for login/authentication)
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- Role-based queries (finding coaches, parents, etc.)
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Active user filtering
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- Phone number lookups
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone_number);

-- ===================
-- Students Table Indexes
-- ===================
-- Active student filtering
CREATE INDEX IF NOT EXISTS idx_students_active ON students(is_active);

-- Skill level filtering
CREATE INDEX IF NOT EXISTS idx_students_skill_level ON students(skill_level);

-- Parent relationship lookups
CREATE INDEX IF NOT EXISTS idx_students_parent ON students(parent_id);

-- Fee status queries
CREATE INDEX IF NOT EXISTS idx_students_fee_status ON students(monthly_fee_status);

-- ===================
-- Batches Table Indexes
-- ===================
-- Coach's batches lookup
CREATE INDEX IF NOT EXISTS idx_batches_coach ON batches(coach_id);

-- Active batch filtering
CREATE INDEX IF NOT EXISTS idx_batches_active ON batches(is_active);

-- Skill level filtering
CREATE INDEX IF NOT EXISTS idx_batches_skill_level ON batches(skill_level);

-- ===================
-- Attendance Table Indexes
-- ===================
-- Date-based queries
CREATE INDEX IF NOT EXISTS idx_attendance_date ON attendance(date);

-- Batch + Date combination (very common query)
CREATE INDEX IF NOT EXISTS idx_attendance_batch_date ON attendance(batch_id, date);

-- Student + Date combination
CREATE INDEX IF NOT EXISTS idx_attendance_student_date ON attendance(student_id, date);

-- Entry type filtering (regular vs makeup)
CREATE INDEX IF NOT EXISTS idx_attendance_entry_type ON attendance(entry_type);

-- ===================
-- Skill Evaluations Table Indexes
-- ===================
-- Student + Date for timeline queries
CREATE INDEX IF NOT EXISTS idx_skill_eval_student_date ON skill_evaluations(student_id, evaluated_at);

-- Coach queries
CREATE INDEX IF NOT EXISTS idx_skill_eval_coach ON skill_evaluations(evaluated_by);

-- Month/Year filtering for reports
CREATE INDEX IF NOT EXISTS idx_skill_eval_month_year ON skill_evaluations(month, year);

-- ===================
-- Achievements Table Indexes
-- ===================
-- Achievement type filtering
CREATE INDEX IF NOT EXISTS idx_achievements_type ON achievements(type);

-- Date-based queries
CREATE INDEX IF NOT EXISTS idx_achievements_date ON achievements(achieved_date);

-- ===================
-- Assessments Table Indexes
-- ===================
-- Student + Date for timeline queries
CREATE INDEX IF NOT EXISTS idx_assessments_student_date ON assessments(student_id, assessment_date);

-- Coach queries
CREATE INDEX IF NOT EXISTS idx_assessments_coach ON assessments(conducted_by);

-- Type filtering
CREATE INDEX IF NOT EXISTS idx_assessments_type ON assessments(type);

-- Date-based queries
CREATE INDEX IF NOT EXISTS idx_assessments_date ON assessments(assessment_date);

-- ===================
-- Attendance Audit Log Indexes
-- ===================
-- Attendance record reference
CREATE INDEX IF NOT EXISTS idx_audit_attendance ON attendance_audit_log(attendance_id);

-- Time-based queries (recent changes)
CREATE INDEX IF NOT EXISTS idx_audit_changed_at ON attendance_audit_log(changed_at);

-- Who made changes
CREATE INDEX IF NOT EXISTS idx_audit_changed_by ON attendance_audit_log(changed_by_id);

-- ===================
-- Fee Payment History Indexes
-- ===================
-- Student fee history
CREATE INDEX IF NOT EXISTS idx_fee_history_student ON fee_payment_history(student_id);

-- Year/Month filtering
CREATE INDEX IF NOT EXISTS idx_fee_history_year_month ON fee_payment_history(year, month);

-- Status filtering
CREATE INDEX IF NOT EXISTS idx_fee_history_status ON fee_payment_history(status);

-- ===================
-- OTP Verifications Indexes
-- ===================
-- Phone number lookups
CREATE INDEX IF NOT EXISTS idx_otp_phone_number ON otp_verifications(phone_number);

-- Expiration checks
CREATE INDEX IF NOT EXISTS idx_otp_expires_at ON otp_verifications(expires_at);

-- ===================
-- Student Training Days Indexes (ElementCollection)
-- ===================
-- Already has primary key index, but adding for compound queries
CREATE INDEX IF NOT EXISTS idx_training_days_student ON student_training_days(student_id);

-- ===================
-- Batch Students (Join Table) Indexes
-- ===================
-- Both sides of the many-to-many relationship
CREATE INDEX IF NOT EXISTS idx_batch_students_batch ON batch_students(batch_id);
CREATE INDEX IF NOT EXISTS idx_batch_students_student ON batch_students(student_id);

-- =====================================================
-- Notes:
-- - All indexes use IF NOT EXISTS for idempotency
-- - Composite indexes are ordered by selectivity
-- - Foreign key indexes improve JOIN performance
-- =====================================================
