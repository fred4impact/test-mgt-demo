CREATE TABLE test_executions (
    id UUID PRIMARY KEY,
    project_id UUID NOT NULL REFERENCES projects(id),
    cycle_id UUID NOT NULL REFERENCES test_cycles(id),
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    assignee_id UUID,
    environment_id UUID NOT NULL REFERENCES environments(id),
    build_id UUID NOT NULL REFERENCES builds(id),
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_RUN',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    duration_ms BIGINT,
    actual_result TEXT,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (cycle_id, test_case_id)
);
