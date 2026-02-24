package com.example.movieapi.service;

import com.example.movieapi.dto.WatchedMovieDto;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

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

    @Test
    void toggleWatched_shouldDeleteRecord_whenAlreadyWatched() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(new AppUser());
        Long  movieId = 1L;
        Movie movie = new Movie();
        movie.setId(movieId);

        WatchedMovie watchedMovie = new WatchedMovie();
        watchedMovie.setMovie(movie);
        watchedMovie.setUser(authenticatedUser.getUser());

        when(movieService.getMovieById(movieId)).thenReturn(movie);

        when(watchedMovieRepository.findByUserAndMovie(authenticatedUser.getUser(), movie)).thenReturn(Optional.of(watchedMovie));

        boolean result = watchedMovieService.toggleWatched(authenticatedUser, movieId);

        assertThat(result).isFalse();
        verify(watchedMovieRepository).delete(watchedMovie);
        verify(watchedMovieRepository, never()).save(watchedMovie);
    }

    @Test
    void toggleWatched_shouldPropagateException_whenMovieNotFound() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(new AppUser());

        when(movieService.getMovieById(anyLong())).thenThrow(RuntimeException.class);

        assertThatThrownBy(() -> watchedMovieService.toggleWatched(authenticatedUser, 1L)).isInstanceOf(RuntimeException.class);

        verifyNoInteractions(watchedMovieRepository);
    }

    @Test
    void getWatchedMovies_shouldReturnEmptyMap_whenUserHasNoWatchedMovies() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(new AppUser());

        when(watchedMovieRepository.findByUserOrderByWatchedAtDesc(authenticatedUser.getUser())).thenReturn(Collections.emptyList());

        Map<LocalDate, List<WatchedMovieDto>> watchedMovies = watchedMovieService.getWatchedMovies(authenticatedUser);

        assertThat(watchedMovies).isEmpty();
    }

    @Test
    void getWatchedMovies_shouldMapEntityToDtoCorrectly() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(new AppUser());
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("title");
        movie.setPosterPath("/path/to/image");
        movie.setRuntime(139);
        movie.setTmdbGenres(Set.of("Action", "Drama"));
        movie.setRating(BigDecimal.valueOf(8.0));
        movie.setCertification("R");
        movie.setReleaseDate(LocalDate.of(2025, 12, 10));
        movie.setVotes(999L);

        WatchedMovie watchedMovie = new WatchedMovie();
        watchedMovie.setMovie(movie);
        watchedMovie.setUser(authenticatedUser.getUser());
        watchedMovie.setWatchedAt(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT));

        when(watchedMovieRepository.findByUserOrderByWatchedAtDesc(authenticatedUser.getUser())).thenReturn(List.of(watchedMovie));

        Map<LocalDate, List<WatchedMovieDto>> watchedMovies = watchedMovieService.getWatchedMovies(authenticatedUser);

        assertThat(watchedMovies).isNotEmpty();

        WatchedMovieDto watchedMovieDto = watchedMovies.get(watchedMovie.getWatchedAt().toLocalDate()).getFirst();

        assertThat(watchedMovieDto.getTitle()).isEqualTo(movie.getTitle());
        assertThat(watchedMovieDto.getPosterPath()).isEqualTo(movie.getPosterPath());
        assertThat(watchedMovieDto.getRating()).isEqualTo(movie.getRating());
        assertThat(watchedMovieDto.getCertification()).isEqualTo(movie.getCertification());
        assertThat(watchedMovieDto.getReleaseDate()).isEqualTo("Dec 10, 2025");
        assertThat(watchedMovieDto.getRuntime()).isEqualTo("2h 19min");
        assertThat(watchedMovieDto.getWatchedAt()).isEqualTo(watchedMovie.getWatchedAt().toLocalDate());
        assertThat(watchedMovieDto.getGenres()).isEqualTo(movie.getTmdbGenres());
        assertThat(watchedMovieDto.getVotes()).isEqualTo(movie.getVotes());
        assertThat(watchedMovieDto.getMovieId()).isEqualTo(String.valueOf(watchedMovie.getMovie().getId()));
    }

    @Test
    void getWatchedMovies_shouldGroupByWatchedDate() {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(new AppUser());

        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("title");
        movie.setPosterPath("/path/to/image");
        movie.setRuntime(139);
        movie.setTmdbGenres(Set.of("Action", "Drama"));
        movie.setRating(BigDecimal.valueOf(8.0));
        movie.setCertification("R");
        movie.setReleaseDate(LocalDate.of(2025, 12, 10));
        movie.setVotes(999L);

        WatchedMovie watchedMovie1 = new WatchedMovie();
        watchedMovie1.setMovie(movie);
        watchedMovie1.setUser(authenticatedUser.getUser());
        watchedMovie1.setWatchedAt(LocalDateTime.of(LocalDate.of(2025, 12, 10), LocalTime.MIDNIGHT));

        WatchedMovie watchedMovie2 = new WatchedMovie();
        watchedMovie2.setMovie(movie);
        watchedMovie2.setUser(authenticatedUser.getUser());
        watchedMovie2.setWatchedAt(LocalDateTime.of(LocalDate.of(2025, 12, 10), LocalTime.MIDNIGHT));

        WatchedMovie watchedMovie3 = new WatchedMovie();
        watchedMovie3.setMovie(movie);
        watchedMovie3.setUser(authenticatedUser.getUser());
        watchedMovie3.setWatchedAt(LocalDateTime.of(LocalDate.of(2025, 12, 12), LocalTime.MIDNIGHT));

        when(watchedMovieRepository.findByUserOrderByWatchedAtDesc(authenticatedUser.getUser())).thenReturn(List.of(watchedMovie1, watchedMovie2, watchedMovie3));

        Map<LocalDate, List<WatchedMovieDto>> watchedMovies = watchedMovieService.getWatchedMovies(authenticatedUser);

        assertThat(watchedMovies).isNotEmpty()
                .containsKeys(LocalDate.of(2025, 12, 10), LocalDate.of(2025, 12, 12));

        List<WatchedMovieDto> watchedList1 = watchedMovies.get(LocalDate.of(2025, 12, 10));
        List<WatchedMovieDto> watchedList2 = watchedMovies.get(LocalDate.of(2025, 12, 12));

        assertThat(watchedList1).hasSize(2);
        assertThat(watchedList2).hasSize(1);
    }

    @Test
    void getWatchedMoviesIds_shouldReturnEmptySet_whenNoneWatched() {
        AppUser user = new AppUser();

        when(watchedMovieRepository.findAllWatchedMoviesByUser(user)).thenReturn(new ArrayList<>());

        Set<Long> watchedMoviesIds = watchedMovieService.getWatchedMoviesIds(user);

        assertThat(watchedMoviesIds).isEmpty();
    }
}