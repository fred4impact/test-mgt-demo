CREATE TABLE test_cases (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    folder_id UUID NOT NULL REFERENCES test_folders(id),
    key VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    priority VARCHAR(50),
    severity VARCHAR(50),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    test_type VARCHAR(50),
    automation_status VARCHAR(50),
    owner_id UUID NOT NULL REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (project_id, key)
);
