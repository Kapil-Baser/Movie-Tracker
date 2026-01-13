package com.example.movieapi.repository;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieSubscriptionRepository extends JpaRepository<MovieSubscription, Long> {
    List<MovieSubscription> findAllByUser(AppUser user);

    @Query("SELECT ms.movie FROM MovieSubscription ms WHERE ms.user = :user")
    List<Movie> findAllSubscribedMoviesByUser(@Param("user") AppUser user);

    Optional<MovieSubscription> findByUserAndMovie(AppUser user, Movie movie);

    List<MovieSubscription> findAllByMovieAndNotifiedFalse(Movie movie);
}
