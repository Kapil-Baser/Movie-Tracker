package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieSubscription;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.MovieSubscriptionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieSubscriptionServiceTest {

    private AppUser appUser;
    private AuthenticatedUser authenticatedUser;
    private Movie movie;
    private Long movieId = 1L;

    @Mock
    private MovieService movieService;
    @Mock
    private MovieSubscriptionRepository movieSubscriptionRepository;
    @InjectMocks
    private MovieSubscriptionService movieSubscriptionService;

    @BeforeEach
    void setUp() {
        appUser = new AppUser();
        authenticatedUser = new AuthenticatedUser(appUser);
        movie = new Movie();
    }

    @Test
    void toggleSubscription_shouldCreateSubscription_whenNotAlreadySubscribed() {

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        when(movieSubscriptionRepository.findByUserAndMovie(appUser, movie)).thenReturn(Optional.empty());

        boolean result = movieSubscriptionService.toggleSubscription(authenticatedUser, movieId);

        ArgumentCaptor<MovieSubscription> captor = ArgumentCaptor.forClass(MovieSubscription.class);
        verify(movieSubscriptionRepository).save(captor.capture());

        MovieSubscription movieSubscription = captor.getValue();

        assertThat(movieSubscription.getUser()).isEqualTo(appUser);
        assertThat(movieSubscription.getMovie()).isEqualTo(movie);
        assertThat(movieSubscription.getSubscribedAt()).isNotNull();
        assertThat(movieSubscription.isNotified()).isFalse();
        assertThat(result).isTrue();

        verify(movieSubscriptionRepository, never()).delete(movieSubscription);
    }

    @Test
    void toggleSubscription_shouldDeleteSubscription_whenAlreadySubscribed() {
        MovieSubscription movieSubscription = new MovieSubscription();
        movieSubscription.setMovie(movie);
        movieSubscription.setUser(appUser);

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        when(movieSubscriptionRepository.findByUserAndMovie(appUser, movie)).thenReturn(Optional.of(movieSubscription));

        boolean result = movieSubscriptionService.toggleSubscription(authenticatedUser, movieId);

        assertThat(result).isFalse();

        verify(movieSubscriptionRepository).delete(movieSubscription);
        verify(movieSubscriptionRepository, never()).save(movieSubscription);
    }

    @Test
    void toggleSubscription_shouldAlwaysLoadMovieById() {

        movieSubscriptionService.toggleSubscription(authenticatedUser, movieId);

        verify(movieService).getMovieById(movieId);
    }

    @Test
    void toggleSubscription_shouldLookupSubscriptionByUserAndMovie() {

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        movieSubscriptionService.toggleSubscription(authenticatedUser, movieId);

        verify(movieSubscriptionRepository).findByUserAndMovie(appUser, movie);
    }

    @Test
    void toggleSubscription_shouldNotCreateDuplicateSubscription_whenAlreadySubscribed() {
        MovieSubscription movieSubscription = new MovieSubscription();
        movieSubscription.setMovie(movie);
        movieSubscription.setUser(appUser);

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        when(movieSubscriptionRepository.findByUserAndMovie(appUser, movie)).thenReturn(Optional.of(movieSubscription));

        movieSubscriptionService.toggleSubscription(authenticatedUser, movieId);

        verify(movieSubscriptionRepository, never()).save(movieSubscription);
    }
}