-- V11: Create activity_logs table for audit logging
CREATE TABLE IF NOT EXISTS activity_logs (
    id          BIGSERIAL PRIMARY KEY,
    action      VARCHAR(50)  NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    details     VARCHAR(1000),
    user_id     BIGINT,
    user_name   VARCHAR(255),
    timestamp   TIMESTAMP    NOT NULL DEFAULT NOW(),
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(500),
    CONSTRAINT fk_activity_logs_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_activity_entity    ON activity_logs (entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_activity_user      ON activity_logs (user_id);
CREATE INDEX IF NOT EXISTS idx_activity_timestamp ON activity_logs (timestamp);
CREATE INDEX IF NOT EXISTS idx_activity_action    ON activity_logs (action);
