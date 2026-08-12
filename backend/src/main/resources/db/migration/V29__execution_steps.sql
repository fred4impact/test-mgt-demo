CREATE TABLE execution_steps (
    id UUID PRIMARY KEY,
    execution_id UUID NOT NULL REFERENCES test_executions(id),
    test_step_id UUID NOT NULL REFERENCES test_steps(id) ON DELETE CASCADE,
    step_number INT NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'NOT_RUN',
    actual_result TEXT,
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (execution_id, test_step_id)
);
