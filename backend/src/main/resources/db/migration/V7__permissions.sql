CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    code VARCHAR(100) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO permissions (id, code, description) VALUES
    (gen_random_uuid(), 'ORG_ADMIN', 'Manage organization settings and membership'),
    (gen_random_uuid(), 'PROJECT_ADMIN', 'Manage a project''s settings and membership'),
    (gen_random_uuid(), 'PROJECT_VIEW', 'View a project and its contents'),
    (gen_random_uuid(), 'TEAM_MANAGE', 'Create teams and manage their membership'),
    (gen_random_uuid(), 'ROLE_MANAGE', 'Create roles and manage their permissions');
