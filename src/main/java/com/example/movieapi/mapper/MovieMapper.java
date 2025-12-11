package com.example.movieapi.mapper;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.dto.MovieResultDto;
import com.example.movieapi.entity.Genre;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.model.PagedResults;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.utility.LanguageUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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

    public List<MovieResultDto> toMovieResultsDto(PagedResults pagedResults) {
        return pagedResults.getMovieResults().stream()
                .map(movieResult -> MovieResultDto.builder()
                        .title(movieResult.getTitle())
                        .overview(movieResult.getOverview())
                        .releaseDate(String.valueOf(movieResult.getReleaseDate()))
                        .language(LanguageUtil.getLanguageName(movieResult.getOriginalLanguage()))
                        .build())
                .toList();
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
                .build();
    }

    public List<MovieDto> toMovieDto(List<Movie> movies) {
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
                .backdropPath(movie.getBackdropPath())
                .posterPath(movie.getPosterPath())
                .usDigitalReleaseDate(String.valueOf(movie.getUsDigitalDate()))
                .releaseDate(movie.getReleaseDate()
                        .format(DateTimeFormatter
                                .ofPattern("MMM d, yyyy", Locale.ENGLISH)))
                .genres(movie.getGenres().stream()
                        .map(Genre::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
