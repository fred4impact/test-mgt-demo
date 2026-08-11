CREATE TABLE test_plans (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    release_id UUID NOT NULL REFERENCES releases(id),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    owner_id UUID NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
