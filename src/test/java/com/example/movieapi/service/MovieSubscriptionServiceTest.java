package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieSubscription;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.MovieSubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieSubscriptionServiceTest {

    private AppUser appUser;
    private AuthenticatedUser authenticatedUser;
    private Movie movie;
    private final Long movieId = 1L;

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

    @Test
    void getSubscribedMovieIds_shouldReturnEmptySet_whenUserHasNoSubscriptions() {
        when(movieSubscriptionRepository.findAllSubscribedMoviesByUser(appUser)).thenReturn(Collections.emptyList());

        Set<Long> result = movieSubscriptionService.getSubscribedMovieIds(appUser);

        assertThat(result).isEmpty();
    }

    @Test
    void getSubscribedMovieIds_shouldReturnMovieIds_whenUserHasSubscriptions() {
        Movie movie1 = new Movie();
        movie1.setId(movieId);
        Movie movie2 = new Movie();
        movie2.setId(2L);
        Movie movie3 = new Movie();
        movie3.setId(3L);
        when(movieSubscriptionRepository.findAllSubscribedMoviesByUser(appUser)).thenReturn(List.of(movie1, movie2, movie3));

        Set<Long> result = movieSubscriptionService.getSubscribedMovieIds(appUser);

        assertThat(result).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void getSubscribedMovieIds_shouldCallRepositoryWithCorrectUser() {
        when(movieSubscriptionRepository.findAllSubscribedMoviesByUser(appUser)).thenReturn(Collections.emptyList());

        movieSubscriptionService.getSubscribedMovieIds(appUser);

        verify(movieSubscriptionRepository).findAllSubscribedMoviesByUser(appUser);
    }
}