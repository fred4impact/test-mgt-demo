CREATE TABLE builds (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    release_id UUID NOT NULL REFERENCES releases(id),
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100),
    branch VARCHAR(255),
    commit_sha VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
