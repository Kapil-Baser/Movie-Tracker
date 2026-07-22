package com.example.movieapi.service;

import com.example.movieapi.dto.*;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.event.MovieEnrichmentEvent;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.response.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.tmdb.model.TmdbMovie;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.model.trakt.response.TraktAllVideosResponse;
import com.example.movieapi.model.trakt.response.TraktMostAnticipatedResponse;
import com.example.movieapi.model.trakt.response.TraktMostWatchedMoviesResponse;
import com.example.movieapi.model.trakt.response.TraktTrendingResponse;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class MovieSyncService {

    private final TmdbService tmdbService;
    private final TraktService traktService;
    private final MovieService movieService;
    private final MovieMapper movieMapper;
    private final MovieCollectionService movieCollectionService;
    private final ApplicationEventPublisher movieEnrichmentEventPublisher;
    private final Executor asyncExecutor;

    @Autowired
    public MovieSyncService(TmdbService tmdbService,
                            TraktService traktService,
                            MovieService movieService,
                            MovieMapper movieMapper,
                            MovieCollectionService movieCollectionService,
                            ApplicationEventPublisher movieEnrichmentEventPublisher,
                            @Qualifier("customExecutor") Executor asyncExecutor) {
        this.tmdbService = tmdbService;
        this.traktService = traktService;
        this.movieService = movieService;
        this.movieMapper = movieMapper;
        this.movieCollectionService = movieCollectionService;
        this.movieEnrichmentEventPublisher = movieEnrichmentEventPublisher;
        this.asyncExecutor = asyncExecutor;
    }

    public List<MovieDto> syncUpcomingMovies(int page) {
        // Fetching the base list from TMDB
        List<TmdbMovie> movieResults = tmdbService.getUpcomingMovies(page);
        log.info("Fetched {} upcoming movies", movieResults.size());

        // Collecting all tmdb ids to check against the database which movies already exist
        List<Long> tmdbIds = movieResults.stream()
                .map(TmdbMovie::getId)
                .toList();

        Map<Long, Movie> existingMoviesMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                        .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));
        log.info("{} movies found with TMDB IDs: {}", existingMoviesMap.size(), existingMoviesMap.keySet());

        List<Long> newMoviesIds = tmdbIds.stream()
                .filter(tmdbId -> !existingMoviesMap.containsKey(tmdbId))
                .toList();
        log.info("Requesting more details for {} new upcoming movies", newMoviesIds.size());

        // Now calling the TMDB API again to fetch details about the not saved movies
        List<TmdbMovieDetailsResponse> newMovies = getMovieDetailsFromTmdbAsync(newMoviesIds);
        log.info("Fetched {} new upcoming movies with TMDB IDs: {}", newMovies.size(), newMoviesIds);

        // Saving the returned movies
        List<Movie> savedMovies = movieService.saveTmdbMoviesWithReleaseDates(newMovies);
        log.info("Saved {} new movies", savedMovies.size());

        // Publish the movie enrichment event to get additional details of movies from Trakt API
        movieEnrichmentEventPublisher.publishEvent(new MovieEnrichmentEvent(savedMovies, "Trakt"));

        // Combining the existing and new saved movies into one list
        List<Movie> upcomingMovies = Stream.concat(existingMoviesMap.values().stream(), savedMovies.stream()).toList();
        log.info("Combined {} movies to be added to upcoming movie collection", upcomingMovies.size());

        movieCollectionService.addToCollection("Upcoming", upcomingMovies);

        return movieMapper.toMovieDto(savedMovies);
    }

    private static List<TmdbMovie> fetchTmdbMovies(int page, Function<Integer, List<TmdbMovie>> getTmdbMovies) {
        List<TmdbMovie> movies = getTmdbMovies.apply(page);
        if (movies == null || movies.isEmpty()) {
            log.warn("TMDI API response is empty");
            return List.of();
        }
        return movies;
    }

    public TmdbSyncCollectionSummary syncUpcomingCollectionFromTmdb(int page) {

        List<TmdbMovie> upcomingMovies = MovieSyncService.fetchTmdbMovies(page, tmdbService::getUpcomingMovies);
        TmdbSyncResult result = fetchAndSyncFromTmdb(upcomingMovies);

        if (!result.allMovies().isEmpty()) {
            movieCollectionService.addToCollection("Upcoming", result.allMovies());
        }
        return TmdbSyncCollectionSummary.builder()
                .totalFetchedFromTmdb(result.totalFetchedFromTmdb())
                .alreadyInDatabase(result.alreadyInDatabase())
                .newlySaved(result.newlySaved())
                .movies(movieMapper.toMovieDto(result.allMovies()))
                .build();
    }

    public TmdbSyncCollectionSummary syncNowPlayingCollectionFromTmdb(int page) {

        List<TmdbMovie> trendingMovies = MovieSyncService.fetchTmdbMovies(page, pageNumber -> tmdbService.getTrendingMovies(pageNumber).getResults());
        TmdbSyncResult result = fetchAndSyncFromTmdb(trendingMovies);

        if (!result.allMovies().isEmpty()) {
            movieCollectionService.addToCollection("Now Playing", result.allMovies());
        }
        return TmdbSyncCollectionSummary.builder()
                .totalFetchedFromTmdb(result.totalFetchedFromTmdb())
                .alreadyInDatabase(result.alreadyInDatabase())
                .newlySaved(result.newlySaved())
                .movies(movieMapper.toMovieDto(result.allMovies()))
                .build();
    }

    /*public TmdbSyncCollectionSummary syncNowPlayingCollectionFromTmdb(int page) {
        TmdbSyncResult result = fetchAndSyncFromTmdb(page);

        if (!result.allMovies().isEmpty()) {
            movieCollectionService.addToCollection("Now Playing", result.allMovies());
        }
        return TmdbSyncCollectionSummary.builder()
                .totalFetchedFromTmdb(result.totalFetchedFromTmdb())
                .alreadyInDatabase(result.alreadyInDatabase())
                .newlySaved(result.newlySaved())
                .movies(movieMapper.toMovieDto(result.allMovies()))
                .build();
    }*/

    private TmdbSyncResult fetchAndSyncFromTmdb(List<TmdbMovie> tmdbMovies) {
        if (tmdbMovies.isEmpty()) {
            log.warn("Could not fetch any movies");
            return TmdbSyncResult.empty();
        }
        log.info("Fetched {} movies from TMDB API", tmdbMovies.size());

        List<Long> tmdbIds = tmdbMovies.stream()
                .map(TmdbMovie::getId)
                .toList();

        Map<Long, Movie> existingMoviesMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));
        log.info("Found {} out of {} movies in database", existingMoviesMap.size(), tmdbIds.size());

        List<Long> newMoviesIds = tmdbIds.stream()
                .filter(tmdbId -> !existingMoviesMap.containsKey(tmdbId))
                .toList();

        List<Movie> newlySavedMovies = newMoviesIds.isEmpty()
                ? List.of()
                : fetchAndSaveMovies(newMoviesIds);

        List<Movie> allMovies = Stream.concat(newlySavedMovies.stream(), existingMoviesMap.values().stream()).toList();

        return TmdbSyncResult.builder()
                .totalFetchedFromTmdb(tmdbIds.size())
                .alreadyInDatabase(existingMoviesMap.size())
                .newlySaved(newlySavedMovies.size())
                .allMovies(allMovies)
                .build();
    }


    /*private TmdbSyncResult fetchAndSyncFromTmdb(int page) {
        TmdbTrendingMoviesResponse nowPlayingMoviesResponse = tmdbService.getTrendingMovies(page);
        if (nowPlayingMoviesResponse == null || nowPlayingMoviesResponse.getResults().isEmpty()) {
            log.warn("Could not fetch any trending movies");
            return TmdbSyncResult.empty();
        }
        log.info("Fetched {} movies from TMDB API", nowPlayingMoviesResponse.getResults().size());

        List<Long> tmdbIds = nowPlayingMoviesResponse.getResults().stream()
                .map(TmdbMovie::getId)
                .toList();

        Map<Long, Movie> existingMoviesMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));
        log.info("Found {} out of {} movies in database", existingMoviesMap.size(), tmdbIds.size());

        List<Long> newMoviesIds = tmdbIds.stream()
                .filter(tmdbId -> !existingMoviesMap.containsKey(tmdbId))
                .toList();

        List<Movie> newlySavedMovies = newMoviesIds.isEmpty()
                ? List.of()
                : fetchAndSaveMovies(newMoviesIds);

        List<Movie> allMovies = Stream.concat(newlySavedMovies.stream(), existingMoviesMap.values().stream()).toList();

        return TmdbSyncResult.builder()
                .totalFetchedFromTmdb(tmdbIds.size())
                .alreadyInDatabase(existingMoviesMap.size())
                .newlySaved(newlySavedMovies.size())
                .allMovies(allMovies)
                .build();
    }*/

    private List<Movie> fetchAndSaveMovies(List<Long> tmdbIds) {
        List<TmdbMovieDetailsResponse> newNowPlayingMoviesResponse = getMovieDetailsFromTmdbAsync(tmdbIds);
        log.info("Fetched {} new trending movies", newNowPlayingMoviesResponse.size());

        List<Movie> savedMovies = movieService.saveTmdbMoviesWithReleaseDates(newNowPlayingMoviesResponse);
        log.info("Saved {} new trending movies", savedMovies.size());

        movieEnrichmentEventPublisher.publishEvent(new MovieEnrichmentEvent(savedMovies, "Trakt"));
        return savedMovies;
    }

    private List<TmdbMovieDetailsResponse> getMovieDetailsFromTmdbAsync(List<Long> tmdbIds) {

        List<CompletableFuture<TmdbMovieDetailsResponse>> futures = tmdbIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> tmdbService.getMovieDetails(id), asyncExecutor)
                        .exceptionally(ex -> {
                            log.info("Failed to fetch movie with TMDB ID: {}, Exception: {}", id, ex.getMessage());
                            return null;
                        }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        return futures.stream()
                .map(CompletableFuture::join)
                .filter(Objects::nonNull)
                .toList();
    }

    public void updateMovieRuntime() {
        List<Movie> moviesWithMissingRuntime = movieService.getMoviesMissingRuntime();
        if (moviesWithMissingRuntime.isEmpty()) {
            log.info("No new movie with missing runtime found");
            return;
        }
        log.info("Movies with missing runtime: {}", moviesWithMissingRuntime.size());

        Map<Long, Movie> moviesMap = moviesWithMissingRuntime.stream()
                .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));

        List<Long> tmdbIds = new ArrayList<>(moviesMap.keySet());

        List<TmdbMovieDetailsResponse> movieDetailsResponses = getMovieDetailsFromTmdbAsync(tmdbIds);

        List<Movie> moviesToUpdate = new ArrayList<>();

        for (TmdbMovieDetailsResponse response : movieDetailsResponses) {
            Movie movie = moviesMap.get(response.getId());
            int runtime = response.getRuntime();
            if (movie != null && runtime > 0) {
                movie.setRuntime(runtime);
                moviesToUpdate.add(movie);
                log.info("Updated movie: {} with runtime: {}", movie.getTitle(), runtime);
            } else {
                log.info("Runtime not found for movie: {}", response.getTitle());
            }
        }

        movieService.saveAll(moviesToUpdate);
    }

    @Transactional
    public void fetchAndSyncDigitalReleaseDates() {
        List<Movie> pendingMovies = movieService.getMoviesWithNoDigitalReleaseDate();
        if (pendingMovies.isEmpty()) {
            log.info("All movies in database have a digital release date already");
            return;
        }
        log.info("There are {} movies with no Digital release date", pendingMovies.size());

        // Calling the .join() here to block the main thread until All background fetches are done.
        List<Movie> moviesToSave = fetchAllMoviesAsync(pendingMovies, asyncExecutor).join();

        if (!moviesToSave.isEmpty()) {
            movieService.saveAll(moviesToSave);
            log.info("Saved {} movies with digital release dates", moviesToSave.size());
        }
    }

    private CompletableFuture<List<Movie>> fetchAllMoviesAsync(List<Movie> movies, Executor executor) {
        List<CompletableFuture<Movie>> futures = movies.stream()
                .map(movie -> fetchAndMapReleaseDateAsync(movie, executor)
                        .exceptionally(ex -> {
                            log.warn("Failed to get released date of movie ID: {}, Exception: {}",movie.getTmdbId(), ex.getMessage());
                            return null;
                        })
                )
                .toList();

        return CompletableFuture
                .allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(_ -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(Objects::nonNull)
                        .toList()
                );
    }

    private CompletableFuture<Movie> fetchAndMapReleaseDateAsync(Movie movie, Executor executor) {
        return CompletableFuture.supplyAsync(() -> safeFetchMovieReleaseDates(movie.getTmdbId()), executor)
                .thenApply(response -> {
                    if (response == null || response.getResults().isEmpty()) {
                        log.info("No released date found for movie: {}", movie.getTitle());
                        return null;
                    }
                    return movieService.setUsDigitalReleaseDate(movie, response.getResults());
                });
    }

    public TmdbMovieDetailsResponse getMovieDetails(Long movieId) {
        return tmdbService.getMovieDetails(movieId);
    }

    private TmdbReleaseDatesResponse safeFetchMovieReleaseDates(Long id) {
        try {
            return tmdbService.getReleaseDatesByMovieId(id);
        } catch (Exception e) {
            log.warn("Failed to fetch details for TMDB ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    public List<MovieDto> syncMostAnticipated() {
        List<TraktMostAnticipatedResponse> anticipatedMoviesResponse = traktService.getAnticipated();
        log.info("Fetched {} most anticipated movies from Trakt API", anticipatedMoviesResponse.size());

        List<TraktMovie> anticipatedMovies = anticipatedMoviesResponse.stream()
                .map(TraktMostAnticipatedResponse::getMovie)
                .toList();

        List<Long> traktIds = anticipatedMovies.stream()
                .map(traktMovie -> traktMovie.getIds().getTrakt())
                .filter(Objects::nonNull)
                .toList();
        log.info("Trakt Ids of most anticipated movies {}", traktIds);

        // Map of existing movies
        Map<Long, Movie> existingMoviesMap = movieService.findAllByTraktIdIn(traktIds).stream()
                .collect(Collectors.toMap(Movie::getTraktId, movie -> movie));
        log.info("Found {} existing movies with Trakt IDs: {}", existingMoviesMap.size(), existingMoviesMap.keySet());

        List<TraktMovie> unsavedMovies = anticipatedMovies.stream()
                .filter(traktMovie -> !existingMoviesMap.containsKey(traktMovie.getIds().getTrakt()))
                .toList();
        log.info("There are {} movies which needs to be saved", unsavedMovies.size());

        List<Movie> savedTraktMovies = movieService.saveTraktMovies(unsavedMovies);

        if (savedTraktMovies.isEmpty()) {
            log.info("No new movies were saved to anticipated collection");
            return List.of();
        }

        log.info("Saved {} new anticipated movies", savedTraktMovies.size());
        // Combine saved and existing movies
        List<Movie> anticipatedMoviesList = Stream.concat(existingMoviesMap.values().stream(), savedTraktMovies.stream()).toList();
        movieCollectionService.addMoviesToCollection("Anticipated", anticipatedMoviesList);
        // Convert to MovieDto and return
        return movieMapper.toMovieDto(anticipatedMoviesList);
    }

    public List<MovieDto> syncTrendingMoviesFromTrakt() {
        List<TraktTrendingResponse> trendingMoviesResponse = traktService.getTrendingMovies();
        log.info("Fetched {} trending movies from Trakt", trendingMoviesResponse.size());

        List<TraktMovie> trendingTraktMovies = trendingMoviesResponse.stream()
                .map(TraktTrendingResponse::getMovie)
                .filter(Objects::nonNull)
                .toList();

        List<Long> tmdbIds = trendingTraktMovies.stream()
                .map(traktMovie -> traktMovie.getIds().getTmdb())
                .filter(Objects::nonNull)
                .toList();
        log.info("TMDB Ids of trending movies: {}", tmdbIds);

        Map<Long, Movie> existingMoviesMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                .collect(Collectors.toMap(Movie::getTmdbId, movie -> movie));
        log.info("Found {} existing movies with TMDB IDs: {}", existingMoviesMap.size(), existingMoviesMap.keySet());

        List<TraktMovie> newTrendingMovies = trendingTraktMovies.stream()
                .filter(traktMovie -> !existingMoviesMap.containsKey(traktMovie.getIds().getTmdb()))
                .toList();
        log.info("There are {} new trending movies", newTrendingMovies.size());

        List<Movie> savedTrendingMovies = movieService.saveTraktMovies(newTrendingMovies);

        if (savedTrendingMovies.isEmpty()) {
            log.info("No new trending movies were saved");
            return List.of();
        }

        List<Movie> trendingMovies = Stream.concat(existingMoviesMap.values().stream(), savedTrendingMovies.stream()).toList();
        movieCollectionService.addToCollection("Trending", trendingMovies);

        movieEnrichmentEventPublisher.publishEvent(new MovieEnrichmentEvent(savedTrendingMovies, "TMDB"));

        return movieMapper.toMovieDto(trendingMovies);
    }

    public YouTubeSyncSummary syncYouTubeTrailers() {
        List<Movie> moviesMissingTrailer = movieService.moviesWithNoTrailers();
        if (moviesMissingTrailer.isEmpty()) {
            log.info("All Movies already have trailers");
        }

        List<CompletableFuture<TrailerFetchResult>> futures = moviesMissingTrailer.stream()
                .map(movie -> CompletableFuture.supplyAsync(() -> {

                    try {
                        List<TraktAllVideosResponse> allVideos =  traktService.getAllVideos(movie.getTraktId());

                        Optional<TraktAllVideosResponse> trailerOpt = allVideos.stream()
                                .filter(v -> "trailer".equals(v.getType()) && v.getTitle().contains("Trailer"))
                                .findFirst();

                        if (trailerOpt.isPresent()) {
                            movie.setTrailer(trailerOpt.get().getUrl());
                            return new TrailerFetchResult(movie, TrailerFetchStatus.UPDATED);
                        }
                        return new TrailerFetchResult(movie, TrailerFetchStatus.NOT_FOUND);

                    } catch (Exception e) {
                        log.error("Error while trying to sync movies with trailers", e);
                        return new TrailerFetchResult(movie, TrailerFetchStatus.FAILED);
                    }

                }, asyncExecutor))
                .toList();

        List<TrailerFetchResult> trailerFetchResults = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(_ -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList())
                .join();

        List<Movie> moviesToUpdate = trailerFetchResults.stream()
                .filter(tfr -> tfr.status().equals(TrailerFetchStatus.UPDATED))
                .map(TrailerFetchResult::movie)
                .toList();

        if (!moviesToUpdate.isEmpty()) {
            movieService.saveAll(moviesToUpdate);
            log.info("Successfully updated trailers for {} movies", moviesToUpdate.size());
        }

        long trailersNotFound = trailerFetchResults.stream()
                .filter(tfr -> tfr.status().equals(TrailerFetchStatus.NOT_FOUND))
                .count();

        long failures = trailerFetchResults.stream()
                .filter(tfr -> tfr.status().equals(TrailerFetchStatus.FAILED))
                .count();

        return YouTubeSyncSummary.builder()
                .moviesScanned(moviesMissingTrailer.size())
                .trailersUpdated(moviesToUpdate.size())
                .trailersNotFound(trailersNotFound)
                .failures(failures)
                .build();
    }

    public void syncMostWatchedMovies(int page) {
        List<TraktMostWatchedMoviesResponse> mostWatchedResponse = traktService.getMostWatchedMovies(page);
        if (mostWatchedResponse.isEmpty()) {
            log.info("No movies found in Trakt Most Watched Movies");
            return;
        }

        List<TraktMovie> mostWatchedMovies = mostWatchedResponse.stream()
                .map(TraktMostWatchedMoviesResponse::getMovie)
                .toList();

        List<Long> tmdbIds = mostWatchedMovies.stream()
                .map(traktMovie -> traktMovie.getIds().getTmdb())
                .toList();

        Map<Long, Movie> existingMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));

        Map<Long, TraktMovie> newTraktMoviesMap = mostWatchedMovies.stream()
                .filter(traktMovie -> !existingMap.containsKey(traktMovie.getIds().getTmdb()))
                .collect(Collectors.toMap(traktMovie ->  traktMovie.getIds().getTmdb(), Function.identity()));
        if (newTraktMoviesMap.isEmpty()) {
            log.info("All movies already exists in DB");
            return;
        }
        log.info("Movies found in Trakt Most Watched Movies: {}, With TMDB IDS: {}", newTraktMoviesMap.size(), newTraktMoviesMap.keySet());

        List<TmdbMovieDetailsResponse> tmdbMovies = getMovieDetailsFromTmdbAsync(new ArrayList<>(newTraktMoviesMap.keySet()));

        List<Movie> savedTmdbMovies = movieService.saveTmdbMoviesWithReleaseDates(tmdbMovies);

        List<Movie> updatedMovies = movieService.updateTraktMovies(new ArrayList<>(newTraktMoviesMap.values()), savedTmdbMovies);
        log.info("Updated {} movies", updatedMovies.size());

        var mostWatchedMoviesToBeAddedToCollection = Stream.concat(existingMap.values().stream(), savedTmdbMovies.stream()).toList();

        movieCollectionService.addToCollection("Now Playing", mostWatchedMoviesToBeAddedToCollection);
    }
}
