-- Migration: Updating the foreign key constraint of movie_collections
-- Version: 13
-- Description: Updating the foreign key constraint of movie_collections table to include ON DELETE CASCADE

-- Set schema context to temp
SET search_path TO movie_tracker_schema;

ALTER TABLE movie_collections
    DROP CONSTRAINT fkhrnrbir7cfbauoqcw4dywsqkq;

ALTER TABLE movie_collections
    ADD CONSTRAINT fk_movies
    FOREIGN KEY (movie_id) REFERENCES movies (id) ON DELETE CASCADE;