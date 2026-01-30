package com.example.movieapi.service;

import com.example.movieapi.dto.WatchedMovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.WatchedMovie;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.WatchedMovieRepository;
import com.example.movieapi.utility.FormatUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WatchedMovieService {

    private final WatchedMovieRepository watchedMovieRepository;
    private final MovieService movieService;

    @Autowired
    public WatchedMovieService(WatchedMovieRepository watchedMovieRepository, MovieService movieService) {
        this.watchedMovieRepository = watchedMovieRepository;
        this.movieService = movieService;
    }

    public boolean toggleWatched(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();
        Movie movie = movieService.getMovieById(movieId);

        Optional<WatchedMovie> optWatchedMovieRecord = watchedMovieRepository.findByUserAndMovie(user, movie);

        if (optWatchedMovieRecord.isPresent()) {
            watchedMovieRepository.delete(optWatchedMovieRecord.get());
            return false;
        } else {
            WatchedMovie watchedMovie = WatchedMovie.builder()
                    .user(user)
                    .movie(movie)
                    .watchedAt(LocalDateTime.now())
                    .build();
            watchedMovieRepository.save(watchedMovie);
            return true;
        }
    }

    public Map<LocalDate, List<WatchedMovieDto>> getWatchedMovies(AuthenticatedUser authenticatedUser) {
        AppUser user = authenticatedUser.getUser();

        List<WatchedMovie> watchedMovies = watchedMovieRepository.findByUserOrderByWatchedAtDesc(user);
        List<WatchedMovieDto> watchedMoviesDto = watchedMovies.stream()
                .map(watchedMovie -> {
                    Movie movie = watchedMovie.getMovie();
                    return WatchedMovieDto.builder()
                            .title(movie.getTitle())
                            .posterPath(movie.getPosterPath())
                            .movieId(String.valueOf(movie.getId()))
                            .watchedAt(watchedMovie.getWatchedAt().toLocalDate())
                            .releaseDate(FormatUtil.formatReleaseDate(movie.getReleaseDate()))
                            .genres(movie.getTmdbGenres())
                            .certification(movie.getCertification())
                            .runtime(FormatUtil.formatRuntime(movie.getRuntime()))
                            .rating(movie.getRating())
                            .votes(movie.getVotes())
                            .build();
                })
                .toList();

        // By default, it was not returning the map in descending order
        return watchedMoviesDto.stream()
                .collect(Collectors.groupingBy(WatchedMovieDto::getWatchedAt, LinkedHashMap::new, Collectors.toList()));
    }

    public Set<Long> getWatchedMoviesIds(AppUser user) {
        List<Movie> watchedMovies = watchedMovieRepository.findAllWatchedMoviesByUser(user);

        return watchedMovies.stream()
                .map(Movie::getId)
                .collect(Collectors.toSet());
    }

    @Transactional
    public void markAsUnwatched(AppUser user, Long movieId) {
        watchedMovieRepository.deleteByUserAndMovieId(user, movieId);
    }

    public int getWatchedMoviesCount(AppUser user, LocalDate watchedAtDate) {
        LocalDateTime startDate = watchedAtDate.atStartOfDay();
        LocalDateTime endDate = watchedAtDate.plusDays(1).atStartOfDay();

        return watchedMovieRepository.findCountByUserAndWatchedAt(user, startDate, endDate);
    }
}
