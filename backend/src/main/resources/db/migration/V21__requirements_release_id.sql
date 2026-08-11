ALTER TABLE requirements ADD COLUMN release_id UUID REFERENCES releases(id);
