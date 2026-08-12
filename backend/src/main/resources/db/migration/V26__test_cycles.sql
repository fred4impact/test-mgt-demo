CREATE TABLE test_cycles (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    test_plan_id UUID NOT NULL REFERENCES test_plans(id),
    release_id UUID NOT NULL REFERENCES releases(id),
    build_id UUID NOT NULL REFERENCES builds(id),
    environment_id UUID NOT NULL REFERENCES environments(id),
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    owner_id UUID NOT NULL,
    start_date DATE,
    end_date DATE,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
