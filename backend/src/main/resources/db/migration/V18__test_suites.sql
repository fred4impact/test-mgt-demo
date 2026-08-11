CREATE TABLE test_suites (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    parent_id UUID REFERENCES test_suites(id),
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
