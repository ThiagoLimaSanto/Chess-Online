CREATE TABLE performances (
    id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    games_played INTEGER NOT NULL DEFAULT 0,
    victory INTEGER NOT NULL DEFAULT 0,
    draw INTEGER NOT NULL DEFAULT 0,
    loss INTEGER NOT NULL DEFAULT 0,
    rating INTEGER NOT NULL DEFAULT 500,

    CONSTRAINT pk_performances PRIMARY KEY (id),
    CONSTRAINT uk_performances_user UNIQUE (user_id),
    CONSTRAINT fk_performance_user
        FOREIGN KEY (user_id) REFERENCES users(id)
);