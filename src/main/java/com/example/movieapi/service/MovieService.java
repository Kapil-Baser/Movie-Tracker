package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDetailsDto;
import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.dto.MovieResultDto;
import com.example.movieapi.entity.Genre;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.*;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.repository.GenresRepository;
import com.example.movieapi.repository.MoviesRepository;
import com.example.movieapi.utility.ReleaseTypeUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    private final TmdbApiClient apiClient;
    private final RestClient client;
    private final MovieMapper movieMapper;
    private final GenresRepository genresRepository;
    private final MoviesRepository moviesRepository;

    @Autowired
    public MovieService(TmdbApiClient apiClient, @Qualifier("tmdbServiceClient") RestClient client, MovieMapper movieMapper, GenresRepository genresRepository, MoviesRepository moviesRepository) {
        this.apiClient = apiClient;
        this.client = client;
        this.movieMapper = movieMapper;
        this.genresRepository = genresRepository;
        this.moviesRepository = moviesRepository;
    }

    public MovieDetailsDto getMovieTopLevelDetailsById(Long movieId) {
        ResponseEntity<TmdbMovie> movieResponseEntity = client.get().uri(uriBuilder -> uriBuilder.path("/movie/" + movieId).build()).retrieve().toEntity(TmdbMovie.class);
        //var movie = apiClient.getMovieById(movieId);
        //return new MovieDetailsDto(movie.getOriginalTitle(), movie.getOverview());
        var movie = movieResponseEntity.getBody();
        return new MovieDetailsDto(movie.getOriginalTitle(), movie.getOverview());
    }

    public PagedResults getMovieByTitle(String title) {
        return client.get()
                .uri(uriBuilder -> uriBuilder.path("/search/movie")
                        .queryParam("query", title)
                        .build())
                .retrieve()
                .body(PagedResults.class);
    }

    public List<MovieResultDto> getTrendingMovies(String timeWindow) {
        PagedResults pagedResults = client.get()
                .uri(uriBuilder -> uriBuilder.path("/trending/movie/" + timeWindow)
                        .build())
                .retrieve()
                .body(PagedResults.class);

        return movieMapper.toMovieResultsDto(Objects.requireNonNull(pagedResults));
    }


    @Transactional
    public List<Movie> saveMovies(List<MovieResultResponse> movieResults) {

        if (movieResults == null || movieResults.isEmpty()) {
            return Collections.emptyList();
        }

        // Extracting all movies ids from API response
        List<Long> movieIds = movieResults.stream()
                .map(MovieResultResponse::getId)
                .toList();

        // Checking which movies already exists in the database
        Set<Long> existingIds = moviesRepository.findAllById(movieIds).stream()
                .map(Movie::getId)
                .collect(Collectors.toSet());

        log.info("Found {} existing movies out of {}", existingIds.size(), movieIds.size());

        // Filtering out existing movies
        List<MovieResultResponse> newMovies = movieResults.stream()
                .filter(movie -> !existingIds.contains(movie.getId()))
                .toList();

        if (newMovies.isEmpty()) {
            log.info("No new movies to save");
            return moviesRepository.findAllById(movieIds);
        }

        // Fetching all required genres IDs
        Set<Integer> genresIds = newMovies.stream()
                .flatMap(movie -> movie.getGenreIds().stream())
                .collect(Collectors.toSet());

        log.info("Fetching genres with IDs: {}", genresIds);

        // Fetching genres from database
        Set<Genre> allGenres = genresRepository.findByIdIn(genresIds);

        log.info("Found {} genres in the database", allGenres.size());

        // Map for quick genre lookup
        Map<Integer, Genre> genreMap = allGenres.stream()
                .collect(Collectors.toMap(Genre::getId, genre -> genre));


        // Map and save all new movies
        List<Movie> moviesToSave = newMovies.stream()
                .map(movieResultResponse -> {
                    Movie movie = movieMapper.toEntity(movieResultResponse);

                    Set<Genre> genresToSave = movieResultResponse.getGenreIds().stream()
                            .map(genreMap::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet());

                    movie.setGenres(genresToSave);
                    return movie;
                })
                .toList();

        // Saving all movies
        List<Movie> savedMovies = moviesRepository.saveAll(moviesToSave);

        log.info("Successfully saved {} movies", savedMovies.size());

        // I am here requesting all the movies returned by the API
        // and not the new movies I could save in the database
        // TODO: make less calls to the database
        return moviesRepository.findAllById(movieIds);
    }

    @Transactional
    public Movie saveMovie(MovieResultResponse movieResult) {

        return moviesRepository.findById(movieResult.getId())
                .orElseGet( () -> {
                    Movie movie = movieMapper.toEntity(movieResult);
                    Set<Genre> genre = genresRepository.findByIdIn(movieResult.getGenreIds());
                    movie.setGenres(genre);
                    return moviesRepository.save(movie);
                });
    }

    public Movie getMovieById(Long movieId) {
        return moviesRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found in database with id: " + movieId));
    }

    @Transactional
    public Movie updateUsReleaseDates(Movie movie, List<TmdbCountryRelease> allReleases) {

        // Here we try to find all release dates for US
        List<TmdbReleaseDate> usReleaseDates = allReleases.stream()
                .filter(cr -> "US".equals(cr.getCountryCode()))
                .map(TmdbCountryRelease::getReleaseDates)
                .flatMap(Collection::stream)
                .toList();

        if (usReleaseDates.isEmpty()) {
            log.warn("No US release dates found for movie: {}", movie.getTitle());
            return movie;
        }

        Movie updatedMovie = processReleaseDates(usReleaseDates, movie);

        Movie savedMovie = moviesRepository.save(updatedMovie);
        log.info("Updated US release dates for movie: {}", savedMovie.getTitle());
        return savedMovie;
    }

    private Movie processReleaseDates(List<TmdbReleaseDate> usReleaseDates, Movie movie) {

        // Find and set Theatrical Date
        usReleaseDates.stream()
                .filter(date -> date.getType() == ReleaseTypeUtil.THEATRICAL)
                .findFirst()
                .ifPresent(releaseDate -> {
                    movie.setUsTheatricalDate(releaseDate.getReleaseDate());
                    movie.setUsCertification(releaseDate.getCertification());
                });

        // Find and set Digital Date
        usReleaseDates.stream()
                .filter(date -> date.getType() == ReleaseTypeUtil.DIGITAL)
                .findFirst()
                .ifPresent(tmdbReleaseDate -> movie.setUsDigitalDate(tmdbReleaseDate.getReleaseDate()));

        // Find and set Physical Date
        usReleaseDates.stream()
                .filter(date -> date.getType() == ReleaseTypeUtil.PHYSICAL)
                .findFirst()
                .ifPresent(tmdbReleaseDate -> movie.setUsPhysicalDate(tmdbReleaseDate.getReleaseDate()));

        return movie;
    }

    public List<Long> getAllMoviesIds() {
        return moviesRepository.findAllMovieIds();
    }

    public List<Movie> getMoviesWithNoDigitalReleaseDate() {
        return moviesRepository.findByUsDigitalDateIsNull();
    }

    public List<MovieDto> getMoviesByKeyword(String keyword) {
        List<Movie> movieList = moviesRepository.findByTitleContainingIgnoreCase(keyword);

        return movieMapper.toMovieDto(movieList);
    }
}
