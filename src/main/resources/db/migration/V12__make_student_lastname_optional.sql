-- Make last_name column optional (nullable) for students
ALTER TABLE students ALTER COLUMN last_name DROP NOT NULL;
