-- =====================================================
-- Manual Security Fix: flyway_schema_history RLS
-- =====================================================
-- This script must be executed manually in Supabase Dashboard
-- after the application deployment completes.
--
-- Why manual? Enabling RLS on flyway_schema_history during a
-- Flyway migration causes timeouts due to exclusive table locks.
-- =====================================================

-- Step 1: Enable Row Level Security
ALTER TABLE public.flyway_schema_history ENABLE ROW LEVEL SECURITY;

-- Step 2: Revoke all public access
REVOKE ALL ON TABLE public.flyway_schema_history FROM PUBLIC;

-- Step 3: Revoke access from Supabase client roles
REVOKE ALL ON TABLE public.flyway_schema_history FROM anon;
REVOKE ALL ON TABLE public.flyway_schema_history FROM authenticated;

-- Step 4: Create policy for service_role access
CREATE POLICY "backend_access_flyway_history" 
ON public.flyway_schema_history
    FOR ALL 
    TO service_role
    USING (true)
    WITH CHECK (true);

-- =====================================================
-- Verification Query
-- =====================================================
-- Run this to verify RLS is enabled and policy is created:
--
-- SELECT 
--     schemaname,
--     tablename,
--     rowsecurity as rls_enabled
-- FROM pg_tables 
-- WHERE tablename = 'flyway_schema_history';
--
-- SELECT policyname, cmd, roles 
-- FROM pg_policies 
-- WHERE tablename = 'flyway_schema_history';
-- =====================================================

-- =====================================================
-- Impact Assessment
-- =====================================================
-- ✅ Flyway migrations: Unaffected (postgres superuser bypasses RLS)
-- ✅ Spring Boot backend: Unaffected (postgres superuser bypasses RLS)
-- ✅ PostgREST API: Blocked for anon/authenticated, allowed for service_role
-- ✅ Security: CRITICAL vulnerability resolved
-- =====================================================
