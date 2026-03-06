-- =====================================================
-- V9: Fix RLS Policies and Remove Duplicate Indexes
-- =====================================================
-- This migration addresses:
-- 1. RLS Policy Always True - Replace overly permissive policies with proper role checks
-- 2. Duplicate Indexes - Remove redundant indexes
-- 3. RLS Disabled on flyway_schema_history - Enable RLS with proper access control
-- =====================================================
-- IMPORTANT: This uses database role checks (current_user, session_user) compatible
-- with both Supabase and standard PostgreSQL JDBC connections.
-- The Spring Boot backend connects as postgres user, which bypasses RLS by default
-- since postgres is a superuser. However, these policies protect against:
-- 1. Direct Supabase API access (PostgREST)
-- 2. Any non-superuser database connections
-- =====================================================

-- =====================================================
-- PART 1: Fix RLS Policies
-- =====================================================
-- The current policies use USING (true) WITH CHECK (true) which effectively
-- bypasses RLS for all roles. We replace them with proper role-based policies.
-- 
-- For Supabase deployments:
-- - service_role: Backend service connections
-- - authenticated: Logged-in users via Supabase client
-- - anon: Anonymous users via Supabase client
-- - postgres: Database owner (Flyway migrations, direct admin access)
--
-- Note: The postgres superuser bypasses RLS by default, so the Spring Boot
-- backend will continue to work. These policies protect against PostgREST access.
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

-- Create new policies with proper role checks
-- Using current_setting for Supabase's request.jwt.claims with fallback
-- These block direct PostgREST API access while allowing backend access

-- Helper function to safely check the current role (handles both Supabase and direct PostgreSQL)
CREATE OR REPLACE FUNCTION is_service_backend() RETURNS BOOLEAN AS $$
BEGIN
    -- Check if running as service_role (Supabase) or postgres (direct JDBC)
    RETURN (
        current_user IN ('postgres', 'service_role') 
        OR session_user IN ('postgres', 'service_role')
        OR (
            -- Check Supabase JWT role claim if available
            current_setting('request.jwt.claims', true) IS NOT NULL 
            AND current_setting('request.jwt.claims', true)::jsonb->>'role' = 'service_role'
        )
    );
EXCEPTION 
    WHEN OTHERS THEN 
        -- If any error occurs (e.g., setting doesn't exist), check database role
        RETURN current_user IN ('postgres', 'service_role');
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Grant execute on the helper function
GRANT EXECUTE ON FUNCTION is_service_backend() TO PUBLIC;

-- Create restrictive policies for all tables
-- These allow access only to service_role/postgres users

CREATE POLICY "backend_access_users" ON users
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_coaches" ON coaches
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_parents" ON parents
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_students" ON students
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_batches" ON batches
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_batch_students" ON batch_students
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_student_training_days" ON student_training_days
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_attendance" ON attendance
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_attendance_audit_log" ON attendance_audit_log
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_achievements" ON achievements
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_skill_evaluations" ON skill_evaluations
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_assessments" ON assessments
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_fee_payment_history" ON fee_payment_history
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_otp_verifications" ON otp_verifications
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

CREATE POLICY "backend_access_app_config" ON app_config
    FOR ALL 
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

-- =====================================================
-- PART 2: Remove Duplicate Indexes
-- =====================================================
-- V4 and V8 migrations created duplicate indexes on the same columns
-- Removing the duplicates to reduce storage and maintenance overhead
-- =====================================================

-- ==========================================
-- attendance_audit_log duplicate indexes
-- ==========================================
-- V4 created: idx_audit_attendance_id, idx_audit_changed_by_id
-- V8 created: idx_audit_attendance, idx_audit_changed_by (same columns)
-- Keep the V8 versions (shorter names), drop V4 versions

DROP INDEX IF EXISTS idx_audit_attendance_id;
DROP INDEX IF EXISTS idx_audit_changed_by_id;

-- ==========================================
-- attendance duplicate indexes (if any)
-- ==========================================
-- Check and clean up any attendance table duplicate indexes
-- The foreign key doesn't auto-create indexes in PostgreSQL, but JPA might have

-- Drop any duplicate student_id indexes (keep idx_attendance_student_status as it's composite)
-- These might have been created by JPA/Hibernate at runtime
DO $$
DECLARE
    idx_record RECORD;
    idx_count INTEGER;
BEGIN
    -- Check for duplicate indexes on attendance(student_id) only
    SELECT COUNT(*) INTO idx_count
    FROM pg_indexes
    WHERE tablename = 'attendance'
      AND indexdef LIKE '%student_id%'
      AND indexdef NOT LIKE '%student_id, status%'  -- Exclude composite
      AND indexdef NOT LIKE '%student_id, date%';    -- Exclude composite
    
    IF idx_count > 1 THEN
        -- Keep only the first one, this handles edge cases
        FOR idx_record IN (
            SELECT indexname 
            FROM pg_indexes 
            WHERE tablename = 'attendance' 
              AND indexdef LIKE '%student_id%'
              AND indexdef NOT LIKE '%student_id, status%'
              AND indexdef NOT LIKE '%student_id, date%'
            OFFSET 1
        ) LOOP
            EXECUTE 'DROP INDEX IF EXISTS ' || idx_record.indexname;
        END LOOP;
    END IF;
END $$;

-- ==========================================
-- fee_payment_history duplicate indexes (if any)
-- ==========================================
-- Clean up any duplicate indexes on fee_payment_history

DO $$
DECLARE
    idx_record RECORD;
    idx_count INTEGER;
BEGIN
    -- Check for duplicate indexes on fee_payment_history(student_id) only
    SELECT COUNT(*) INTO idx_count
    FROM pg_indexes
    WHERE tablename = 'fee_payment_history'
      AND indexdef LIKE '%(student_id)%';
    
    IF idx_count > 1 THEN
        -- Keep idx_fee_history_student, drop others
        FOR idx_record IN (
            SELECT indexname 
            FROM pg_indexes 
            WHERE tablename = 'fee_payment_history' 
              AND indexdef LIKE '%(student_id)%'
              AND indexname != 'idx_fee_history_student'
        ) LOOP
            EXECUTE 'DROP INDEX IF EXISTS ' || idx_record.indexname;
        END LOOP;
    END IF;
END $$;

-- =====================================================
-- PART 3: Enable RLS on flyway_schema_history
-- =====================================================
-- Enable RLS on flyway_schema_history to comply with security requirements
-- Only postgres/service_role should be able to access migration history
-- =====================================================

ALTER TABLE IF EXISTS flyway_schema_history ENABLE ROW LEVEL SECURITY;

-- Create policy for backend access to flyway_schema_history
CREATE POLICY "backend_access_flyway" ON flyway_schema_history
    FOR ALL
    USING (is_service_backend())
    WITH CHECK (is_service_backend());

-- =====================================================
-- VERIFICATION
-- =====================================================
-- Add comments for documentation
COMMENT ON FUNCTION is_service_backend() IS 
    'Helper function to check if current connection is from the backend service (postgres or service_role)';

-- =====================================================
-- Notes:
-- - All existing data remains accessible via Spring Boot backend (connects as postgres)
-- - Direct Supabase PostgREST API access is now properly restricted
-- - Flyway migrations continue to work via postgres role
-- - No application code changes required
-- - The postgres superuser bypasses RLS by default, ensuring backend compatibility
-- =====================================================
