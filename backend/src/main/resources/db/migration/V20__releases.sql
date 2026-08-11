CREATE TABLE releases (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    name VARCHAR(255) NOT NULL,
    version VARCHAR(100),
    status VARCHAR(50) NOT NULL DEFAULT 'PLANNED',
    start_date DATE,
    release_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
