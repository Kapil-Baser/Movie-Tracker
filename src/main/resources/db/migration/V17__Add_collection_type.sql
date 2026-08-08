-- Migration: Adding collection type to collections
-- Version: 17
-- Description: Schema for adding collection type field to the movie collections,
-- Adding DEFAULT value as 'CUSTOM' to update the existing rows then removing it after.

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

ALTER TABLE movie_collection
ADD COLUMN type varchar(30) NOT NULL DEFAULT 'CUSTOM';

ALTER TABLE movie_collection
ALTER COLUMN type DROP DEFAULT;