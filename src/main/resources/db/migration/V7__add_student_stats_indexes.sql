-- Speeds up student listing stats aggregation queries
CREATE INDEX IF NOT EXISTS idx_attendance_student_status
    ON attendance(student_id, status);

CREATE INDEX IF NOT EXISTS idx_achievements_student_verified
    ON achievements(student_id, is_verified);

CREATE INDEX IF NOT EXISTS idx_skill_evaluations_student
    ON skill_evaluations(student_id);
