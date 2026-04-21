-- Migration: Create movie collections table
-- Version: 8
-- Description: Initial schema creation for movie collections table

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

CREATE TABLE IF NOT EXISTS movie_collections (
    collection_id bigint not null,
    movie_id bigint not null,
    primary key (collection_id, movie_id)
);



-- Add comment to table
COMMENT ON TABLE movie_tracker_schema.movie_collections IS 'Stores movie collections information';