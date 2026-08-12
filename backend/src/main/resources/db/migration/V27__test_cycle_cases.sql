CREATE TABLE test_cycle_cases (
    id UUID PRIMARY KEY,
    cycle_id UUID NOT NULL REFERENCES test_cycles(id),
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    assignee_id UUID,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now()
);
