CREATE TABLE test_suite_cases (
    id UUID PRIMARY KEY,
    suite_id UUID NOT NULL REFERENCES test_suites(id),
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (suite_id, test_case_id)
);
