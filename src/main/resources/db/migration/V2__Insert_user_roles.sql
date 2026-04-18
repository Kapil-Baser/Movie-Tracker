-- Migration: Insert roles into roles table
-- Version: 2
-- Description: Inserting user roles

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

INSERT INTO roles (name)
    VALUES ('ROLE_ADMIN'),
           ('ROLE_USER');
