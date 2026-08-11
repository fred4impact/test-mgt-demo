CREATE TABLE test_case_tags (
    id UUID PRIMARY KEY,
    test_case_id UUID NOT NULL REFERENCES test_cases(id),
    tag_id UUID NOT NULL REFERENCES tags(id),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (test_case_id, tag_id)
);
