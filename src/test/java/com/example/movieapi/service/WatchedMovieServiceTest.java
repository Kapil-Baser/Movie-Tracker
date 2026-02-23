package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.WatchedMovie;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.WatchedMovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WatchedMovieServiceTest {

    @Mock
    WatchedMovieRepository watchedMovieRepository;
    @Mock
    MovieService movieService;
    @InjectMocks
    WatchedMovieService watchedMovieService;

    @Test
    void toggleWatched_shouldCreateRecord_whenMovieNotYetWatched() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(new AppUser());
        Long  movieId = 1L;
        Movie movie = new Movie();
        movie.setId(movieId);

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        when(watchedMovieRepository.findByUserAndMovie(authenticatedUser.getUser(), movie)).thenReturn(Optional.empty());

        boolean result = watchedMovieService.toggleWatched(authenticatedUser, movieId);

        assertThat(result).isTrue();

        ArgumentCaptor<WatchedMovie> captor = ArgumentCaptor.forClass(WatchedMovie.class);

        verify(watchedMovieRepository).save(captor.capture());

        WatchedMovie watchedMovie = captor.getValue();

        assertThat(watchedMovie).isNotNull();
        assertThat(watchedMovie.getMovie()).isEqualTo(movie);
        assertThat(watchedMovie.getUser()).isEqualTo(authenticatedUser.getUser());
        assertThat(watchedMovie.getWatchedAt()).isNotNull();

        verify(watchedMovieRepository, never()).delete(watchedMovie);
    }
}