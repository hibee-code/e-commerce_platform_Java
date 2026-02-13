INSERT INTO roles (name, created_at, updated_at)
VALUES ('ROLE_USER', now(), now())
ON CONFLICT (name) DO NOTHING;

INSERT INTO roles (name, created_at, updated_at)
VALUES ('ROLE_ADMIN', now(), now())
ON CONFLICT (name) DO NOTHING;
