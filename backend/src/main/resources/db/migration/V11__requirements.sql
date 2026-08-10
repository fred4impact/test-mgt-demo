CREATE TABLE requirements (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    key VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    priority VARCHAR(50),
    owner_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (project_id, key)
);
