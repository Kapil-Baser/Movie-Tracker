package com.example.movieapi.repository;

import com.example.movieapi.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MoviesRepository extends JpaRepository<Movie, Long> {

    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    List<Movie> findByUsDigitalDateIsNull();

    List<Movie> findAllByTrailerIsNull();

    @Query("SELECT m FROM Movie m WHERE m.runtime = 0")
    List<Movie> findMoviesMissingRuntime();

    @Query("SELECT m.id FROM Movie m")
    List<Long> findAllMovieIds();

    @Query("SELECT m FROM MovieCollection c JOIN c.movies m WHERE c.id = :collectionId")
    Page<Movie> findMoviesByCollectionId(@Param("collectionId") Long collectionId, Pageable pageable);

    @Query("SELECT m FROM MovieCollection mc JOIN mc.movies m WHERE mc.name = :collectionName")
    Page<Movie> findMoviesByCollectionNameContainingIgnoreCase(String collectionName, Pageable pageable);

    List<Movie> findAllByUsDigitalDate(LocalDate date);

    boolean existsByTmdbId(Long tmdbId);

    List<Movie> findAllByTmdbIdIn(List<Long> tmdbIds);

    List<Movie> findAllByTraktIdIn(List<Long> traktIds);

    @Query("SELECT m.title FROM Movie m WHERE m.id = :movieId")
    String findTitleByMovieId(Long movieId);

    @Query(value = """
            SELECT * FROM movies
            WHERE :keyword <% title
            ORDER BY similarity(title, :keyword) DESC
            """, nativeQuery = true)
    Page<Movie> fuzzySearch(@Param("keyword") String keyword, Pageable pageable);

    @Query(value = """
        SELECT title FROM Movies
        WHERE :keyword <% title
        ORDER BY title <-> :keyword ASC
        LIMIT 5
    """, nativeQuery = true)
    List<String> activeSearch(@Param("keyword") String keyword);
}
