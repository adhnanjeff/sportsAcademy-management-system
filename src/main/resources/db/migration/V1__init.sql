-- Flyway Migration V1: Initial Schema and Data
-- Schema-first migration so fresh databases can run Flyway without Hibernate DDL.

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    national_id_number VARCHAR(255) UNIQUE,
    date_of_birth DATE NOT NULL,
    photo_url VARCHAR(255),
    phone_number VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    role VARCHAR(50) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    is_email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS coaches (
    user_id BIGINT PRIMARY KEY,
    specialization VARCHAR(255),
    years_of_experience INTEGER,
    bio VARCHAR(1000),
    certifications VARCHAR(255),
    CONSTRAINT fk_coaches_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS parents (
    user_id BIGINT PRIMARY KEY,
    CONSTRAINT fk_parents_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS students (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    gender VARCHAR(20),
    national_id_number VARCHAR(255),
    date_of_birth DATE,
    photo_url VARCHAR(255),
    phone_number VARCHAR(255),
    address VARCHAR(255),
    city VARCHAR(255),
    state VARCHAR(255),
    country VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    skill_level VARCHAR(30) DEFAULT 'BEGINNER',
    fee_payable NUMERIC(10,2) DEFAULT 0,
    monthly_fee_status VARCHAR(20) DEFAULT 'UNPAID',
    parent_id BIGINT,
    CONSTRAINT fk_students_parent FOREIGN KEY (parent_id) REFERENCES parents(user_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS batches (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    skill_level VARCHAR(30),
    coach_id BIGINT NOT NULL,
    start_time TIME,
    end_time TIME,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_batches_coach FOREIGN KEY (coach_id) REFERENCES coaches(user_id)
);

CREATE TABLE IF NOT EXISTS batch_students (
    batch_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (batch_id, student_id),
    CONSTRAINT fk_batch_students_batch FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_batch_students_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS student_training_days (
    student_id BIGINT NOT NULL,
    day_of_week VARCHAR(20) NOT NULL,
    PRIMARY KEY (student_id, day_of_week),
    CONSTRAINT fk_student_training_days_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS attendance (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    batch_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    notes VARCHAR(255),
    marked_by BIGINT,
    marked_at TIMESTAMP,
    CONSTRAINT uk_attendance_student_batch_date UNIQUE (student_id, batch_id, date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_batch FOREIGN KEY (batch_id) REFERENCES batches(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_marked_by FOREIGN KEY (marked_by) REFERENCES coaches(user_id)
);

CREATE TABLE IF NOT EXISTS achievements (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    type VARCHAR(50) NOT NULL,
    event_name VARCHAR(255),
    position VARCHAR(255),
    achieved_date DATE,
    certificate_url VARCHAR(255),
    awarded_by VARCHAR(255),
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by BIGINT,
    CONSTRAINT fk_achievements_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_achievements_verified_by FOREIGN KEY (verified_by) REFERENCES coaches(user_id)
);

CREATE TABLE IF NOT EXISTS skill_evaluations (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    evaluated_by BIGINT NOT NULL,
    footwork INTEGER NOT NULL,
    strokes INTEGER NOT NULL,
    stamina INTEGER NOT NULL,
    attack INTEGER NOT NULL,
    defence INTEGER NOT NULL,
    agility INTEGER NOT NULL,
    court_coverage INTEGER NOT NULL,
    notes VARCHAR(1000),
    evaluated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_skill_evaluations_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_skill_evaluations_evaluated_by FOREIGN KEY (evaluated_by) REFERENCES coaches(user_id)
);

CREATE TABLE IF NOT EXISTS assessments (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    conducted_by BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    name VARCHAR(255) NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    unit VARCHAR(255),
    target_score DOUBLE PRECISION,
    assessment_date DATE NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMP,
    CONSTRAINT fk_assessments_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_assessments_conducted_by FOREIGN KEY (conducted_by) REFERENCES coaches(user_id)
);

CREATE TABLE IF NOT EXISTS fee_payment_history (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    year INTEGER NOT NULL,
    month INTEGER NOT NULL,
    month_name VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    amount_paid NUMERIC(10,2) DEFAULT 0,
    fee_payable NUMERIC(10,2) DEFAULT 0,
    paid_date DATE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    CONSTRAINT uk_fee_payment_history_student_year_month UNIQUE (student_id, year, month),
    CONSTRAINT fk_fee_payment_history_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS otp_verifications (
    id BIGSERIAL PRIMARY KEY,
    phone_number VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    attempt_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS app_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

