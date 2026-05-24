-- V10: Add email field to students table
ALTER TABLE students ADD COLUMN IF NOT EXISTS email VARCHAR(255);

-- Optional: add an index for faster email lookups
CREATE INDEX IF NOT EXISTS idx_students_email ON students(email);
