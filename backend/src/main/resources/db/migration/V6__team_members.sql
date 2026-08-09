CREATE TABLE team_members (
    id UUID PRIMARY KEY,
    team_id UUID NOT NULL REFERENCES teams(id),
    user_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (team_id, user_id)
);
