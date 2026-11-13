-- Migration: Adding a not null constraint to created_at column
-- Version: 18
-- Description: Schema for altering the movies table to add the not null constraint on created_at column

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

ALTER TABLE movies
ALTER COLUMN created_at SET NOT NULL;
