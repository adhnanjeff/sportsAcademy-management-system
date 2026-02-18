-- Step 1: ROLLBACK the failed transaction
ROLLBACK;

-- Step 2: Drop the students table and related constraints
DROP TABLE IF EXISTS students CASCADE;

-- Step 3: That's it! Now restart your Spring Boot application.
-- Hibernate will recreate the students table with the correct schema (no user_id column).

-- The new schema will have:
-- - id BIGINT PRIMARY KEY (not user_id)
-- - first_name, last_name, full_name
-- - date_of_birth, national_id_number, phone_number
-- - skill_level, parent_id (FK to users)
-- - address, city, state, country, photo_url
-- - is_active, created_at, updated_at
-- - fee_payable, monthly_fee_status, gender
