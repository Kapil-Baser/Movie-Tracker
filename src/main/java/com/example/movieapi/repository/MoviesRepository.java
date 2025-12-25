package com.example.movieapi.repository;

import com.example.movieapi.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MoviesRepository extends JpaRepository<Movie, Long> {

    List<Movie> findByTitleContainingIgnoreCase(String title);

    List<Movie> findByUsDigitalDateIsNull();

    @Query("SELECT m.id FROM Movie m")
    List<Long> findAllMovieIds();

    @Query("SELECT m FROM MovieCollection c JOIN c.movies m WHERE c.id = :collectionId")
    Page<Movie> findMoviesByCollectionId(@Param("collectionId") Long collectionId, Pageable pageable);

    boolean existsByTmdbId(Long tmdbId);

    List<Movie> findAllByTmdbIdIn(List<Long> tmdbIds);

    List<Movie> findAllByTraktIdIn(List<Long> traktIds);
}
