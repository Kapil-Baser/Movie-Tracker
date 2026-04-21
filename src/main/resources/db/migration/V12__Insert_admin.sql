-- Migration: Create admin user
-- Version: 12
-- Description: Creating the admin user

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

INSERT INTO users (email, enabled, password, provider, username, role_id, created_at)
VALUES ('admin@email.com', true, '$2a$12$.Wn7Gr40AhD7YiAX.IQD3eFAbX.tUKDpuxebG5z3r94/xyHawtmVS', 'LOCAL', 'admin', 1, NOW());