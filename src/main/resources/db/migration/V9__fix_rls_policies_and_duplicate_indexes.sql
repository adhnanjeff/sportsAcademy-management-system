-- =====================================================
-- V9: Fix RLS Policies and Remove Duplicate Indexes
-- =====================================================
-- This migration addresses:
-- 1. RLS Policy Always True - Replace overly permissive policies with proper role checks
-- 2. Duplicate Indexes - Remove redundant indexes
-- 3. RLS Disabled on flyway_schema_history - Enable RLS with proper access control
-- =====================================================
-- IMPORTANT: The Spring Boot backend connects as postgres user, which bypasses 
-- RLS by default since postgres is a superuser. These policies protect against:
-- 1. Direct Supabase API access (PostgREST) via anon/authenticated keys
-- 2. Any non-superuser database connections
-- =====================================================

-- =====================================================
-- PART 1: Fix RLS Policies
-- =====================================================
-- The current policies use USING (true) WITH CHECK (true) which effectively
-- bypasses RLS for all roles. We replace them with role-based policies that
-- only allow service_role access (and postgres superuser bypasses RLS anyway).
-- =====================================================

-- Drop existing overly permissive policies
DROP POLICY IF EXISTS "Service role has full access to users" ON users;
DROP POLICY IF EXISTS "Service role has full access to coaches" ON coaches;
DROP POLICY IF EXISTS "Service role has full access to parents" ON parents;
DROP POLICY IF EXISTS "Service role has full access to students" ON students;
DROP POLICY IF EXISTS "Service role has full access to batches" ON batches;
DROP POLICY IF EXISTS "Service role has full access to batch_students" ON batch_students;
DROP POLICY IF EXISTS "Service role has full access to student_training_days" ON student_training_days;
DROP POLICY IF EXISTS "Service role has full access to attendance" ON attendance;
DROP POLICY IF EXISTS "Service role has full access to attendance_audit_log" ON attendance_audit_log;
DROP POLICY IF EXISTS "Service role has full access to achievements" ON achievements;
DROP POLICY IF EXISTS "Service role has full access to skill_evaluations" ON skill_evaluations;
DROP POLICY IF EXISTS "Service role has full access to assessments" ON assessments;
DROP POLICY IF EXISTS "Service role has full access to fee_payment_history" ON fee_payment_history;
DROP POLICY IF EXISTS "Service role has full access to otp_verifications" ON otp_verifications;
DROP POLICY IF EXISTS "Service role has full access to app_config" ON app_config;

-- Ensure service_role exists in local/non-Supabase environments
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'service_role') THEN
        CREATE ROLE service_role NOLOGIN;
    END IF;
END
$$;

-- Create new policies that restrict to service_role only
-- These use a simple role check that works in Supabase PostgreSQL
-- The TO clause specifies which roles the policy applies to

CREATE POLICY "backend_access_users" ON users
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_coaches" ON coaches
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_parents" ON parents
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_students" ON students
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_batches" ON batches
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_batch_students" ON batch_students
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_student_training_days" ON student_training_days
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_attendance" ON attendance
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_attendance_audit_log" ON attendance_audit_log
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_achievements" ON achievements
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_skill_evaluations" ON skill_evaluations
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_assessments" ON assessments
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_fee_payment_history" ON fee_payment_history
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_otp_verifications" ON otp_verifications
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

CREATE POLICY "backend_access_app_config" ON app_config
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

-- =====================================================
-- PART 2: Remove Duplicate Indexes
-- =====================================================
-- V4 and V8 migrations created duplicate indexes on the same columns
-- Removing the duplicates to reduce storage and maintenance overhead
-- =====================================================

-- attendance_audit_log: idx_audit_attendance_id (V4) duplicates idx_audit_attendance (V8)
DROP INDEX IF EXISTS idx_audit_attendance_id;

-- attendance_audit_log: idx_audit_changed_by_id (V4) duplicates idx_audit_changed_by (V8)
DROP INDEX IF EXISTS idx_audit_changed_by_id;

-- =====================================================
-- PART 3: flyway_schema_history RLS
-- =====================================================
-- NOTE: flyway_schema_history is managed by Flyway and typically not exposed
-- via PostgREST. If you need to enable RLS on it, do so manually via Supabase
-- dashboard AFTER this migration completes, as modifying it during migration
-- can cause timeout issues.
--
-- To manually enable RLS on flyway_schema_history:
-- 1. Go to Supabase Dashboard > Database > Tables
-- 2. Find flyway_schema_history
-- 3. Enable RLS and add a policy for service_role access
-- =====================================================

-- =====================================================
-- Notes:
-- - All existing data remains accessible via Spring Boot backend 
--   (postgres superuser bypasses RLS by default)
-- - Direct Supabase API access via anon/authenticated keys is blocked
-- - Only service_role key can access data via PostgREST
-- - Flyway migrations continue to work via postgres role
-- - No application code changes required
-- =====================================================
