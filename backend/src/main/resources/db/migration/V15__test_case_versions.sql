CREATE TABLE test_case_versions (
    id UUID PRIMARY KEY,
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    version_number INTEGER NOT NULL,
    snapshot TEXT NOT NULL,
    change_summary VARCHAR(2000),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (test_case_id, version_number)
);
