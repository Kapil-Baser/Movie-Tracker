package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieSubscription;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.MovieSubscriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieSubscriptionService {

    private final MovieSubscriptionRepository subscriptionRepository;
    private final MovieService movieService;

    @Autowired
    public MovieSubscriptionService(MovieSubscriptionRepository subscriptionRepository, MovieService movieService) {
        this.subscriptionRepository = subscriptionRepository;
        this.movieService = movieService;
    }

    public boolean toggleSubscription(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();
        Movie movie = movieService.getMovieById(movieId);

        Optional<MovieSubscription> optMovieSubscription = subscriptionRepository.findByUserAndMovie(user, movie);

        if (optMovieSubscription.isPresent()) {
            subscriptionRepository.delete(optMovieSubscription.get());
            return false;
        } else {
            MovieSubscription movieSubscription = MovieSubscription.builder()
                    .user(user)
                    .movie(movie)
                    .subscribedAt(LocalDateTime.now())
                    .notified(false)
                    .build();
            subscriptionRepository.save(movieSubscription);
            return true;
        }
    }


    public Set<Long> getSubscribedMovieIds(AppUser user) {
        List<Movie> subscribedMovies = subscriptionRepository.findAllSubscribedMoviesByUser(user);

        return subscribedMovies.stream()
                .map(Movie::getId)
                .collect(Collectors.toSet());
    }
}
