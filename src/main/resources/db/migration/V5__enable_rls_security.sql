-- Migration: Enable Row Level Security on all tables
-- This prevents direct access via Supabase API while allowing backend service role access

-- Enable RLS on all application tables
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE coaches ENABLE ROW LEVEL SECURITY;
ALTER TABLE parents ENABLE ROW LEVEL SECURITY;
ALTER TABLE students ENABLE ROW LEVEL SECURITY;
ALTER TABLE batches ENABLE ROW LEVEL SECURITY;
ALTER TABLE batch_students ENABLE ROW LEVEL SECURITY;
ALTER TABLE student_training_days ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance ENABLE ROW LEVEL SECURITY;
ALTER TABLE attendance_audit_log ENABLE ROW LEVEL SECURITY;
ALTER TABLE achievements ENABLE ROW LEVEL SECURITY;
ALTER TABLE skill_evaluations ENABLE ROW LEVEL SECURITY;
ALTER TABLE assessments ENABLE ROW LEVEL SECURITY;
ALTER TABLE fee_payment_history ENABLE ROW LEVEL SECURITY;
ALTER TABLE otp_verifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE app_config ENABLE ROW LEVEL SECURITY;

-- Create policies to allow full access for the postgres/service role
-- These policies allow the Spring Boot backend (using service role) to access all data

CREATE POLICY "Service role has full access to users" ON users
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to coaches" ON coaches
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to parents" ON parents
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to students" ON students
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to batches" ON batches
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to batch_students" ON batch_students
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to student_training_days" ON student_training_days
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to attendance" ON attendance
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to attendance_audit_log" ON attendance_audit_log
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to achievements" ON achievements
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to skill_evaluations" ON skill_evaluations
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to assessments" ON assessments
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to fee_payment_history" ON fee_payment_history
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to otp_verifications" ON otp_verifications
    FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Service role has full access to app_config" ON app_config
    FOR ALL USING (true) WITH CHECK (true);

-- Note: flyway_schema_history is managed by Flyway and should not have RLS
-- as it needs to be accessible for migrations
