CREATE TABLE IF NOT EXISTS tournaments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    batch_id BIGINT NOT NULL,
    created_by BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    total_rounds INTEGER,
    total_participants INTEGER,
    start_date DATE NOT NULL,
    end_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tournaments_batch FOREIGN KEY (batch_id) REFERENCES batches(id),
    CONSTRAINT fk_tournaments_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS matches (
    id BIGSERIAL PRIMARY KEY,
    player1_id BIGINT NOT NULL,
    player2_id BIGINT,
    winner_id BIGINT,
    match_type VARCHAR(30) NOT NULL DEFAULT 'SINGLES',
    player1_score VARCHAR(255),
    player2_score VARCHAR(255),
    score_display VARCHAR(255),
    tournament_id BIGINT,
    round_number INTEGER,
    match_position INTEGER,
    partner1_id BIGINT,
    partner2_id BIGINT,
    match_date DATE NOT NULL,
    duration_minutes INTEGER,
    notes VARCHAR(1000),
    location VARCHAR(255),
    event_name VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_matches_player1 FOREIGN KEY (player1_id) REFERENCES students(id),
    CONSTRAINT fk_matches_player2 FOREIGN KEY (player2_id) REFERENCES students(id),
    CONSTRAINT fk_matches_winner FOREIGN KEY (winner_id) REFERENCES students(id),
    CONSTRAINT fk_matches_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    CONSTRAINT fk_matches_partner1 FOREIGN KEY (partner1_id) REFERENCES students(id),
    CONSTRAINT fk_matches_partner2 FOREIGN KEY (partner2_id) REFERENCES students(id)
);

CREATE TABLE IF NOT EXISTS tournament_participants (
    tournament_id BIGINT NOT NULL,
    student_id BIGINT NOT NULL,
    PRIMARY KEY (tournament_id, student_id),
    CONSTRAINT fk_tp_tournament FOREIGN KEY (tournament_id) REFERENCES tournaments(id) ON DELETE CASCADE,
    CONSTRAINT fk_tp_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE
);
