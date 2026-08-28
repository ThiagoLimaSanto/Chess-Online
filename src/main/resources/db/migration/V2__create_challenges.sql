CREATE TABLE challenges (
    id BINARY(16) NOT NULL,
    challenger_id BINARY(16) NOT NULL,
    challenged_id BINARY(16) NOT NULL,
    status ENUM('PENDING', 'ACCEPTED', 'REJECTED') NOT NULL,
    initial_time_seconds BIGINT NOT NULL,
    increment_seconds BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_challenges PRIMARY KEY (id),
    CONSTRAINT fk_challenges_challenger
        FOREIGN KEY (challenger_id) REFERENCES users(id),
    CONSTRAINT fk_challenges_challenged
        FOREIGN KEY (challenged_id) REFERENCES users(id),
    CONSTRAINT chk_challenges_different_users
        CHECK (challenger_id <> challenged_id),
    INDEX idx_challenges_challenger_status
        (challenger_id, status),
    INDEX idx_challenges_challenged_status
        (challenged_id, status)
);