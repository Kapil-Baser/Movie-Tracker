package com.example.movieapi.repository;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.WatchedMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WatchedMovieRepository extends JpaRepository<WatchedMovie, Long> {

    List<WatchedMovie> findByUserOrderByWatchedAtDesc(AppUser user);

    List<WatchedMovie> findAllByUser(AppUser user);

    @Query("SELECT wm.movie FROM WatchedMovie wm WHERE wm.user = :user")
    List<Movie> findAllWatchedMoviesByUser(@Param("user") AppUser user);

    List<WatchedMovie> findByUserAndWatchedAtBetween(AppUser user, LocalDateTime start, LocalDateTime end);

    Optional<WatchedMovie> findByUserAndMovie(AppUser user, Movie movie);
}
