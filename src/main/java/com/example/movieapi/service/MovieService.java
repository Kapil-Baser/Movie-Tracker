package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.Genre;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.*;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.repository.GenresRepository;
import com.example.movieapi.repository.MoviesRepository;
import com.example.movieapi.utility.ReleaseTypeUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieService {

    private final MovieMapper movieMapper;
    private final GenresRepository genresRepository;
    private final MoviesRepository moviesRepository;

    @Autowired
    public MovieService(MovieMapper movieMapper, GenresRepository genresRepository, MoviesRepository moviesRepository) {
        this.movieMapper = movieMapper;
        this.genresRepository = genresRepository;
        this.moviesRepository = moviesRepository;
    }

    public void updateMoviesWithTraktMovieDetails(List<TraktMovie> traktMovies) {
        if (traktMovies == null || traktMovies.isEmpty()) {
            log.info("No trakt movies to sync");
            return;
        }

        // Get all TMDB Ids
        List<Long> tmdbIds = traktMovies.stream()
                .map(traktMovie -> traktMovie.getIds().getTmdb())
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Movie> existingMovieMap = moviesRepository.findAllByTmdbIdIn(tmdbIds).stream()
                .collect(Collectors.toMap(Movie::getTmdbId, movie -> movie));

        List<Movie> updatedMovies = new ArrayList<>();
        List<Long> missingIds = new ArrayList<>();

        // Updating movies
        for (TraktMovie traktMovie: traktMovies) {
            Long tmdbId = traktMovie.getIds().getTmdb();
            Movie existingMovie = existingMovieMap.get(tmdbId);

            if (existingMovie != null) {
                existingMovie.setCertification(traktMovie.getCertification());
                existingMovie.setRating(traktMovie.getRating());
                existingMovie.setVotes(traktMovie.getVotes());
                existingMovie.setTraktId(traktMovie.getIds().getTrakt());
                existingMovie.setTrailer(traktMovie.getTrailer());
                existingMovie.setTagline(traktMovie.getTagLine());
                existingMovie.setAfterCredits(traktMovie.isHasAfterCredits());
                existingMovie.setDuringCredits(traktMovie.isHasDuringCredits());
                updatedMovies.add(existingMovie);
            } else {
                missingIds.add(tmdbId);
            }
        }

        // Saving back
        moviesRepository.saveAll(updatedMovies);

        log.info("Synced {} movies from trakt", updatedMovies.size());
        if (!missingIds.isEmpty()) {
            log.debug("Movies not found for TMDB IDs: {}", missingIds);
        }
    }

    @Transactional
    public List<Movie> updateTraktMovies(List<TraktMovie> traktMovies, List<Movie> moviesToUpdate) {
        if (traktMovies == null || traktMovies.isEmpty()) {
            log.info("No trakt movies to update");
            return List.of();
        }
        // Making a map for quicker lookup
        Map<String, Movie> imdbMovieMap = moviesToUpdate.stream()
                .collect(Collectors.toMap(Movie::getImdbId, Function.identity()));

        List<Movie> moviesToBeUpdated = new ArrayList<>();

        for (TraktMovie traktMovie: traktMovies) {
            Movie movie = imdbMovieMap.get(traktMovie.getIds().getImdb());
            if (movie != null) {
                movie.setTrailer(traktMovie.getTrailer());
                movie.setTraktId(traktMovie.getIds().getTrakt());
                movie.setTagline(traktMovie.getTagLine());
                movie.setCertification(traktMovie.getCertification());
                movie.setRating(traktMovie.getRating());
                movie.setVotes(traktMovie.getVotes());
                movie.setAfterCredits(traktMovie.isHasAfterCredits());
                movie.setDuringCredits(traktMovie.isHasDuringCredits());
                moviesToBeUpdated.add(movie);
            }
        }
        return moviesRepository.saveAll(moviesToBeUpdated);
    }

    public List<Movie> updateTmdbMovies(List<TmdbMovieDetailsResponse> tmdbMovies, List<Movie> moviesToUpdate) {
        if (tmdbMovies == null || tmdbMovies.isEmpty()) {
            log.info("No TMDB movies to update");
            return List.of();
        }
        // Making a map for quicker lookup
        Map<Long, Movie> tmdbMovieMap = moviesToUpdate.stream()
                .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));

        List<Movie> moviesToBeUpdated = new ArrayList<>();

        for (TmdbMovieDetailsResponse tmdbMovie : tmdbMovies) {
            Movie movie = tmdbMovieMap.get(tmdbMovie.getId());
            if (movie != null) {
                List<TmdbReleaseDate> usReleaseDates = getUsReleaseDates(tmdbMovie.getReleaseDates().getResults());
                if (!usReleaseDates.isEmpty()) {
                    processReleaseDates(usReleaseDates, movie);
                }
                movie.setOverview(tmdbMovie.getOverview());
                movie.setPosterPath(tmdbMovie.getPosterPath());
                movie.setBackdropPath(tmdbMovie.getBackdropPath());
                movie.setTmdbGenres(tmdbMovie.getGenres().stream()
                        .map(TmdbGenre::getName)
                        .collect(Collectors.toSet()));
                moviesToBeUpdated.add(movie);
            }
        }
        return moviesRepository.saveAll(moviesToBeUpdated);
    }

    public List<Movie> saveTraktMovies(List<TraktMovie> traktMovies) {
        if (traktMovies == null || traktMovies.isEmpty()) {
            log.info("No trakt movie to save");
            return List.of();
        }

        List<Movie> unsavedMovies = traktMovies.parallelStream()
                .map(movieMapper::toEntity)
                .toList();

        // Saving new trakt movies
        return moviesRepository.saveAll(unsavedMovies);
    }

    @Transactional
    public List<Movie> saveTmdbMoviesWithReleaseDates(List<TmdbMovieDetailsResponse> movieResults) {
        if (movieResults == null || movieResults.isEmpty()) {
            log.info("Tmdb Api response is empty");
            return List.of();
        }

        List<Movie> movies = new ArrayList<>();

        // Getting us release dates
        for (TmdbMovieDetailsResponse movieResult : movieResults) {
            List<TmdbReleaseDate> usReleaseDates = getUsReleaseDates(movieResult.getReleaseDates().getResults());
            Movie movie = movieMapper.toEntity(movieResult);
            if (!usReleaseDates.isEmpty()) {
                Movie movieWithReleaseDates = processReleaseDates(usReleaseDates, movie);
                movies.add(movieWithReleaseDates);
            } else {
                log.info("Movie with IMDB ID: {} does not have a US release date", movieResult.getImdbId());
                movies.add(movie);
            }
        }

        return moviesRepository.saveAll(movies);
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

    public Movie saveMovie(TraktMovie traktMovie) {
        Movie movie = movieMapper.toEntity(traktMovie);
        return moviesRepository.save(movie);
    }

    public Movie getMovieById(Long movieId) {
        return moviesRepository.findById(movieId)
                .orElseThrow(() -> new RuntimeException("Movie not found in database with id: " + movieId));
    }

    private List<TmdbReleaseDate> getUsReleaseDates(List<TmdbCountryRelease> allReleases) {
        // Here we try to find all release dates for US
        return allReleases.stream()
                .filter(cr -> "US".equals(cr.getCountryCode()))
                .map(TmdbCountryRelease::getReleaseDates)
                .flatMap(Collection::stream)
                .toList();
    }

    private List<TmdbReleaseDate> getUsDigitalReleaseDates(List<TmdbCountryRelease> allReleases) {
        return allReleases.stream()
                .filter(cr -> "US".equals(cr.getCountryCode()))
                .flatMap(r -> r.getReleaseDates().stream())
                .filter(d -> d.getType() == ReleaseTypeUtil.DIGITAL)
                .toList();
    }

    @Transactional
    public Movie updateUsReleaseDates(Movie movie, List<TmdbCountryRelease> allReleases) {

        List<TmdbReleaseDate> usReleaseDates = getUsReleaseDates(allReleases);

        if (usReleaseDates.isEmpty()) {
            log.warn("No US release dates found for movie: {}", movie.getTitle());
            return movie;
        }

        Movie updatedMovie = processReleaseDates(usReleaseDates, movie);

        Movie savedMovie = moviesRepository.save(updatedMovie);
        log.info("Updated US release dates for movie: {}", savedMovie.getTitle());
        return savedMovie;
    }

    public Movie setUsReleaseDates(Movie movieWithNoDigitalReleaseDate, List<TmdbCountryRelease> allReleases) {
        List<TmdbReleaseDate> usReleaseDates = getUsReleaseDates(allReleases);
        if (usReleaseDates.isEmpty()) {
            return null;
        }
        log.info("Set US release dates for movie: {}", movieWithNoDigitalReleaseDate.getTitle());
        return processReleaseDates(usReleaseDates, movieWithNoDigitalReleaseDate);
    }

    public Movie setUsDigitalReleaseDate(Movie movie, List<TmdbCountryRelease> allReleases) {
        Optional<TmdbReleaseDate> digitalRelease = getUsReleaseDates(allReleases).stream()
                .filter(r -> r.getType() == ReleaseTypeUtil.DIGITAL)
                .findFirst();

        if (digitalRelease.isPresent()) {
            log.info("Set US release date for movie: {}", movie.getTitle());
            movie.setUsDigitalDate(digitalRelease.get().getReleaseDate());
            return movie;
        }

        log.info("No US release date found for movie: {}", movie.getTitle());
        return null;
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

    public List<Movie> moviesOutForStreamingToday() {
        return moviesRepository.findAllByUsDigitalDate(LocalDate.now());
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

    public boolean existsByTmdbId(Long tmdbId) {
        return moviesRepository.existsByTmdbId(tmdbId);
    }

    public List<Movie> findAllByTmdbIdIn(List<Long> tmdbIds) {
        return moviesRepository.findAllByTmdbIdIn(tmdbIds);
    }

    public List<Movie> findAllByTraktIdIn(List<Long> traktIds) {
        return moviesRepository.findAllByTraktIdIn(traktIds);
    }

    public List<Movie> saveAll(List<Movie> movies) {
        return moviesRepository.saveAll(movies);
    }
}
