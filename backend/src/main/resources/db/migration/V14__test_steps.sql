CREATE TABLE test_steps (
    id UUID PRIMARY KEY,
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    step_number INTEGER NOT NULL,
    action VARCHAR(2000) NOT NULL,
    test_data VARCHAR(2000),
    expected_result VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (test_case_id, step_number)
);
