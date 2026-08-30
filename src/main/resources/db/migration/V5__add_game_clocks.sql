ALTER TABLE games
    ADD COLUMN white_time_millis BIGINT NULL,
    ADD COLUMN black_time_millis BIGINT NULL,
    ADD COLUMN turn_started_at DATETIME(6) NULL;

UPDATE games
SET white_time_millis = initial_time_seconds * 1000,
    black_time_millis = initial_time_seconds * 1000,
    turn_started_at = COALESCE(created_at, CURRENT_TIMESTAMP(6));

ALTER TABLE games
    MODIFY white_time_millis BIGINT NOT NULL,
    MODIFY black_time_millis BIGINT NOT NULL,
    MODIFY turn_started_at DATETIME(6) NOT NULL;
