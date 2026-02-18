-- Quick fix for development: Drop and recreate schema
-- WARNING: This will delete all data!

-- Drop all tables (in correct order to avoid foreign key issues)
DROP VIEW IF EXISTS student_full_details CASCADE;
DROP TABLE IF EXISTS assessment_metrics CASCADE;
DROP TABLE IF EXISTS assessments CASCADE;
DROP TABLE IF EXISTS skill_evaluations CASCADE;
DROP TABLE IF EXISTS achievements CASCADE;
DROP TABLE IF EXISTS fee_payment_history CASCADE;
DROP TABLE IF EXISTS attendance CASCADE;
DROP TABLE IF EXISTS student_training_days CASCADE;
DROP TABLE IF EXISTS batch_students CASCADE;
DROP TABLE IF EXISTS batches CASCADE;
DROP TABLE IF EXISTS students CASCADE;
DROP TABLE IF EXISTS coaches CASCADE;
DROP TABLE IF EXISTS parents CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS otp_verification CASCADE;
DROP TABLE IF EXISTS app_config CASCADE;

-- Now restart your Spring Boot application with spring.jpa.hibernate.ddl-auto=create or update
-- Hibernate will recreate all tables with the correct schema
