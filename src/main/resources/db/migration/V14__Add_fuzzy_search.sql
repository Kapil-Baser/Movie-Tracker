-- Migration: Adding the fuzzy search capability using POSTGRESQL's pg_trgm extension.
-- Version: 14
-- Description: Enabling POSTGRESQL's trigram extension and adding a Generalized Inverted Index (GIN)

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

-- 1. Enable the trigram extension
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- 2. Create a Trigram index on the title column (for fuzzy/typo search)
CREATE INDEX idx_movies_title_trgm ON movies USING gin (title gin_trgm_ops);