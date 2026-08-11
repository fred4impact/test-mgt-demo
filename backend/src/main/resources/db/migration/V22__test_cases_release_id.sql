ALTER TABLE test_cases ADD COLUMN release_id UUID REFERENCES releases(id);
