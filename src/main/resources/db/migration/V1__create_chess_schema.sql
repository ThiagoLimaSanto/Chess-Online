CREATE TABLE users (
    id BINARY(16) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
);

CREATE TABLE games (
    id BINARY(16) NOT NULL,
    white_player_id BINARY(16) NOT NULL,
    black_player_id BINARY(16) NOT NULL,
    status ENUM('IN_PROGRESS', 'FINISHED') NOT NULL,
    result ENUM('DRAW', 'WHITE_WIN', 'BLACK_WIN'),
    created_at DATETIME(6) NOT NULL,
    finished_at DATETIME(6),
    initial_time_seconds BIGINT NOT NULL,
    increment_seconds BIGINT NOT NULL,
    CONSTRAINT pk_games PRIMARY KEY (id),
    CONSTRAINT fk_games_white_player FOREIGN KEY (white_player_id) REFERENCES users (id),
    CONSTRAINT fk_games_black_player FOREIGN KEY (black_player_id) REFERENCES users (id),
    CONSTRAINT chk_games_different_players CHECK (white_player_id <> black_player_id),
    INDEX idx_games_status_white_player (status, white_player_id),
    INDEX idx_games_status_black_player (status, black_player_id)
);

CREATE TABLE moves (
    id BINARY(16) NOT NULL,
    game_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    from_square VARCHAR(255),
    to_square VARCHAR(255),
    promotion_type ENUM('PAWN', 'ROOK', 'KNIGHT', 'BISHOP', 'QUEEN', 'KING'),
    move_number INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    CONSTRAINT pk_moves PRIMARY KEY (id),
    CONSTRAINT fk_moves_game FOREIGN KEY (game_id) REFERENCES games (id),
    CONSTRAINT fk_moves_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uk_moves_game_move_number UNIQUE (game_id, move_number),
    INDEX idx_moves_game_move_number (game_id, move_number)
);
