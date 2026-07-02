-- =====================================================
-- V14: Enable RLS on tables added after V9
-- =====================================================
-- V11 (activity_logs) and V13 (matches, tournaments,
-- tournament_participants) created tables without enabling
-- Row Level Security, leaving them readable/writable via the
-- Supabase PostgREST API with the anon key
-- (Supabase advisor: rls_disabled_in_public).
--
-- Same model as V9: the Spring Boot backend connects as the
-- postgres role (table owner, bypasses RLS); PostgREST access
-- is restricted to service_role only.
--
-- This migration is idempotent so it can be applied manually to
-- production ahead of the next deploy without breaking Flyway.
-- =====================================================

ALTER TABLE activity_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE matches ENABLE ROW LEVEL SECURITY;
ALTER TABLE tournaments ENABLE ROW LEVEL SECURITY;
ALTER TABLE tournament_participants ENABLE ROW LEVEL SECURITY;

-- Ensure service_role exists in local/non-Supabase environments
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'service_role') THEN
        CREATE ROLE service_role NOLOGIN;
    END IF;
END
$$;

DROP POLICY IF EXISTS "backend_access_activity_logs" ON activity_logs;
CREATE POLICY "backend_access_activity_logs" ON activity_logs
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

DROP POLICY IF EXISTS "backend_access_matches" ON matches;
CREATE POLICY "backend_access_matches" ON matches
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

DROP POLICY IF EXISTS "backend_access_tournaments" ON tournaments;
CREATE POLICY "backend_access_tournaments" ON tournaments
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);

DROP POLICY IF EXISTS "backend_access_tournament_participants" ON tournament_participants;
CREATE POLICY "backend_access_tournament_participants" ON tournament_participants
    FOR ALL
    TO service_role
    USING (true)
    WITH CHECK (true);
