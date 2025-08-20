CREATE TABLE game (
    id SERIAL PRIMARY KEY,
    game_id UUID UNIQUE NOT NULL,
    board jsonb NOT NULL,
    players jsonb NOT NULL,
    last_player_marker VARCHAR NOT NULL,
    winner VARCHAR NOT NULL,
    game_state VARCHAR NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
