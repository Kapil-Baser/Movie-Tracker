-- Migration: Adding a composite index on created_at and id in movies table
-- Version: 19
-- Description: Schema for creating a composite index on movies table to help with keyset pagination sort

SET search_path TO movie_tracker_schema;

CREATE INDEX idx_movies_created_at_id
ON movies (created_at DESC, id ASC);
