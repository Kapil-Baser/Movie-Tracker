package com.example.movieapi.mapper;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.dto.WatchedMovieDto;
import com.example.movieapi.entity.Genre;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.WatchedMovie;
import com.example.movieapi.model.TmdbGenre;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.utility.FormatUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MovieMapper {

    @Value("${image.base.url}")
    private String imageBaseUrl;

    public Movie toEntity(TraktMovie traktMovie) {
        return Movie.builder()
                .tmdbId(traktMovie.getIds().getTmdb())
                .traktId(traktMovie.getIds().getTrakt())
                .imdbId(traktMovie.getIds().getImdb())
                .title(traktMovie.getTitle())
                .originalTitle(traktMovie.getOriginalTitle())
                .tagline(traktMovie.getTagLine())
                .runtime(traktMovie.getRuntime())
                .releaseDate(traktMovie.getReleased())
                .votes(traktMovie.getVotes())
                .rating(traktMovie.getRating())
                .certification(traktMovie.getCertification())
                .trailer(traktMovie.getTrailer())
                .duringCredits(traktMovie.isHasDuringCredits())
                .afterCredits(traktMovie.isHasAfterCredits())
                .build();
    }


    public Movie toEntity(MovieResultResponse movieResult) {
        return Movie.builder()
                .id(movieResult.getId())
                .title(movieResult.getTitle())
                .originalTitle(movieResult.getOriginalTitle())
                .overview(movieResult.getOverview())
                .backdropPath(movieResult.getBackdropPath())
                .posterPath(movieResult.getPosterPath())
                .releaseDate(movieResult.getReleaseDate())
                .createdAt(LocalDateTime.now())
                .afterCredits(false)
                .duringCredits(false)
                .build();
    }

    public Movie toEntity(TmdbMovieDetailsResponse tmdbMovies) {
        return Movie.builder()
                .title(tmdbMovies.getTitle())
                .originalTitle(tmdbMovies.getOriginalTitle())
                .overview(tmdbMovies.getOverview())
                .tmdbId(tmdbMovies.getId())
                .imdbId(tmdbMovies.getImdbId())
                .runtime(tmdbMovies.getRuntime())
                .tmdbGenres(tmdbMovies.getGenres().stream().map(TmdbGenre::getName).collect(Collectors.toSet()))
                .posterPath(tmdbMovies.getPosterPath())
                .backdropPath(tmdbMovies.getBackdropPath())
                .releaseDate(tmdbMovies.getReleaseDate())
                .afterCredits(false)
                .duringCredits(false)
                .build();
    }

    public List<MovieDto> toMovieDto(List<Movie> movies) {
        return movies.parallelStream()
                .map(movie -> MovieDto.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .overview(movie.getOverview())
                        .trailer(movie.getTrailer())
                        .backdropPath(movie.getBackdropPath())
                        .posterPath(movie.getPosterPath())
                        .usDigitalReleaseDate(FormatUtil.formatReleaseDate(movie.getUsDigitalDate()))
                        .releaseDate(FormatUtil.formatReleaseDate(movie.getReleaseDate()))
                        .genres(movie.getTmdbGenres())
                        .build()
                ).toList();
    }


    public List<MovieDto> toMovieDto(Set<Movie> movies) {
        return movies.stream()
                .map(movie -> MovieDto.builder()
                        .id(movie.getId())
                        .title(movie.getTitle())
                        .overview(movie.getOverview())
                        .backdropPath(movie.getBackdropPath())
                        .posterPath(movie.getPosterPath())
                        .usDigitalReleaseDate(String.valueOf(movie.getUsDigitalDate()))
                        .releaseDate(movie.getReleaseDate()
                                .format(DateTimeFormatter
                                        .ofPattern("MMM d, yyyy", Locale.ENGLISH)))
                        .genres(movie.getGenres().stream()
                                .map(Genre::getName)
                                .collect(Collectors.toSet()))
                        .build()
                ).toList();
    }

    public MovieDto toMovieDto (Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .overview(movie.getOverview())
                .imdbId(movie.getImdbId())
                .trailer(movie.getTrailer())
                .backdropPath(movie.getBackdropPath())
                .posterPath(movie.getPosterPath())
                .runtime(FormatUtil.formatRuntime(movie.getRuntime()))
                .usDigitalReleaseDate(FormatUtil.formatReleaseDate(movie.getUsDigitalDate()))
                .releaseDate(FormatUtil.formatReleaseDate(movie.getReleaseDate()))
                .genres(movie.getTmdbGenres())
                .build();
    }

    public WatchedMovieDto toWatchedMovieDto(WatchedMovie watchedMovie) {
        Movie movie = watchedMovie.getMovie();
        return WatchedMovieDto.builder()
                .title(movie.getTitle())
                .movieId(String.valueOf(movie.getId()))
                .posterPath(movie.getPosterPath())
                .releaseDate(releaseDateFormatter(movie.getReleaseDate()))
                .watchedAt(watchedMovie.getWatchedAt().toLocalDate())
                .build();
    }

    // TODO: make other methods use this formatter too
    private String releaseDateFormatter(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH));
    }
}
