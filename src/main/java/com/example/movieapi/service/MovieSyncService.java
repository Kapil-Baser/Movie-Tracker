package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.event.MovieEnrichmentEvent;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.tmdb.model.TmdbCountryRelease;
import com.example.movieapi.model.response.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.response.TmdbTrendingMoviesResponse;
import com.example.movieapi.model.tmdb.model.TmdbMovie;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.model.trakt.response.TraktAllVideosResponse;
import com.example.movieapi.model.trakt.response.TraktMostAnticipatedResponse;
import com.example.movieapi.model.trakt.response.TraktTrendingResponse;
import com.example.movieapi.utility.ReleaseTypeUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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

    @Autowired
    public MovieSyncService(TmdbService tmdbService, TraktService traktService, MovieService movieService, MovieMapper movieMapper, MovieCollectionService movieCollectionService, ApplicationEventPublisher movieEnrichmentEventPublisher) {
        this.tmdbService = tmdbService;
        this.traktService = traktService;
        this.movieService = movieService;
        this.movieMapper = movieMapper;
        this.movieCollectionService = movieCollectionService;
        this.movieEnrichmentEventPublisher = movieEnrichmentEventPublisher;
    }

    private Executor getMovieExecutor(int threads) {
        return Executors.newFixedThreadPool(threads, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public List<MovieDto> syncUpcomingMovies(int page) {
        // Fetching the base list from TMDB
        List<MovieResultResponse> movieResults = tmdbService.getUpcomingMovies(page);
        log.info("Fetched {} upcoming movies", movieResults.size());

        // Collecting all tmdb ids to check against the database which movies already exist
        List<Long> tmdbIds = movieResults.stream()
                .map(MovieResultResponse::getId)
                .filter(Objects::nonNull)
                .toList();

        Map<Long, Movie> existingMoviesMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                        .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));
        log.info("{} movies found with TMDB IDs: {}", existingMoviesMap.size(), existingMoviesMap.keySet());

        List<Long> newMoviesIds = tmdbIds.stream()
                .filter(tmdbId -> !existingMoviesMap.containsKey(tmdbId))
                .toList();
        log.info("Requesting more details for {} new upcoming movies", newMoviesIds.size());

        // Now calling the TMDB API again to fetch details about the not saved movies
        /*List<TmdbMovieDetailsResponse> newMovies = newMoviesIds.parallelStream()
                .map(this::safeFetchMovieDetails)
                .filter(Objects::nonNull)
                .toList();*/
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

        return movieMapper.toMovieDto(upcomingMovies);
    }

    @Transactional
    public List<MovieDto> syncNowPlayingMovies() {

        LocalDate currentDate = LocalDate.now();

        List<MovieResultResponse> movieResults = tmdbService.discoverMovies(currentDate.getYear(), currentDate.minusMonths(2), ReleaseTypeUtil.THEATRICAL);

        // Saving the movies which are new and does not yet exist in the database
        List<Movie> nowPlayingMovies = movieService.saveMovies(movieResults);

        // Making a new now playing collection
        movieCollectionService.addMoviesToCollection("Now Playing", nowPlayingMovies);

        return movieMapper.toMovieDto(nowPlayingMovies);
    }

    public List<MovieDto> syncNowPlayingMovies(int page) {
        // Collecting results from TMDB first
        TmdbTrendingMoviesResponse nowPlayingMoviesResponse = tmdbService.getTrendingMovies(page);
        log.info("Fetched {} movies from TMDB api", nowPlayingMoviesResponse.getResults().size());

        // Filter movies which are not saved
        List<Long> notSavedMovieIds = nowPlayingMoviesResponse.getResults().stream()
                .map(TmdbMovie::getId)
                .filter(tmdbId -> !movieService.existsByTmdbId(tmdbId))
                .toList();

        // Make a details api call to fetch imdb ids and digital release dates
        List<TmdbMovieDetailsResponse> tmdbMovieWithDetails = notSavedMovieIds.stream()
                .map(tmdbService::getMovieDetails)
                .toList();

        List<Movie> nowPlayingMovies = movieService.saveTmdbMoviesWithReleaseDates(tmdbMovieWithDetails);
        log.info("Saved {} movies from TMDB", nowPlayingMovies.size());

        // Extract all imdbIds for trakt api call
        List<String> imdbIds = tmdbMovieWithDetails.stream()
                .map(TmdbMovieDetailsResponse::getImdbId)
                .toList();

        // Get extended movie details from trakt api
        List<TraktMovie> traktMovieWithDetails = imdbIds.stream()
                .map(traktService::getExtendedMovieDetails)
                .toList();
        log.info("Fetched {} movies with extended information from Trakt", traktMovieWithDetails.size());

        movieService.updateMoviesWithTraktMovieDetails(traktMovieWithDetails);

        // Making a new now playing collection
        movieCollectionService.addMoviesToCollection("Now Playing", nowPlayingMovies);

        return nowPlayingMovies.stream()
                .map(movieMapper::toMovieDto)
                .toList();
    }

    public List<MovieDto> syncNowPlayingMoviesFromTmdb(int page) {
        TmdbTrendingMoviesResponse nowPlayingMoviesResponse = tmdbService.getTrendingMovies(page);
        if (nowPlayingMoviesResponse == null || nowPlayingMoviesResponse.getResults().isEmpty()) {
            log.info("Could not fetch any trending movies");
            return List.of();
        }
        log.info("Fetched {} movies from TMDB API", nowPlayingMoviesResponse.getResults().size());

        List<Long> tmdbIds = nowPlayingMoviesResponse.getResults().stream()
                .map(TmdbMovie::getId)
                .toList();

        Map<Long, Movie> existingMoviesMap = movieService.findAllByTmdbIdIn(tmdbIds).stream()
                .collect(Collectors.toMap(Movie::getTmdbId, Function.identity()));
        log.info("Found {} out of {} movies in database", existingMoviesMap.size(), tmdbIds.size());

        List<Long> newMovies = tmdbIds.stream()
                .filter(tmdbId -> !existingMoviesMap.containsKey(tmdbId))
                .toList();

        if (newMovies.isEmpty()) {
            log.info("No new movie that needs to be saved");
            return List.of();
        }

        List<TmdbMovieDetailsResponse> newNowPlayingMoviesResponse = getMovieDetailsFromTmdbAsync(newMovies);
        log.info("Fetched {} new trending movies", newNowPlayingMoviesResponse.size());

        List<Movie> nowPlayingMovies = movieService.saveTmdbMoviesWithReleaseDates(newNowPlayingMoviesResponse);
        log.info("Saved {} new trending movies", nowPlayingMovies.size());

        movieEnrichmentEventPublisher.publishEvent(new MovieEnrichmentEvent(nowPlayingMovies, "Trakt"));

        List<Movie> nowPlayingMoviesCollection = Stream.concat(nowPlayingMovies.stream(), existingMoviesMap.values().stream()).toList();

        movieCollectionService.addToCollection("Now Playing", nowPlayingMoviesCollection);

        return movieMapper.toMovieDto(nowPlayingMovies);
    }

    private List<TmdbMovieDetailsResponse> getMovieDetailsFromTmdbAsync(List<Long> tmdbIds) {
        Executor movieExecutor = getMovieExecutor(Math.min(tmdbIds.size(), 30));

        List<CompletableFuture<TmdbMovieDetailsResponse>> futures = tmdbIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> tmdbService.getMovieDetails(id), movieExecutor)
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

    public List<MovieDto> syncDigitalReleaseDates() {
        // Getting only the movies which do not have a digital release date
        List<Movie> movies = movieService.getMoviesWithNoDigitalReleaseDate();

        List<Movie> updatedMovies = movies.stream()
                .map(this::fetchReleaseDateFromTmdbAndUpdate)
                .toList();

        log.info("Updated total {} movies", updatedMovies.size());

        return movieMapper.toMovieDto(updatedMovies);
    }

    @Transactional
    public void fetchAndSyncDigitalReleaseDates() {
        List<Movie> pendingMovies = movieService.getMoviesWithNoDigitalReleaseDate();
        if (pendingMovies.isEmpty()) {
            log.info("All movies in database have a digital release date already");
            return;
        }
        log.info("There are {} movies with no Digital release date", pendingMovies.size());

        Executor executor = getMovieExecutor(Math.min(pendingMovies.size(), 30));

        CompletableFuture<List<Movie>> allMoviesFuture = fetchAllMoviesAsync(pendingMovies, executor)
                .thenCompose(movies -> saveMoviesAsync(movies, executor));

        allMoviesFuture.join(); // wait for everything to finish before exiting since this method is transactional
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
                .thenApply(v -> futures.stream()
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

    private CompletableFuture<List<Movie>> saveMoviesAsync(List<Movie> movies, Executor executor) {
        if (movies == null || movies.isEmpty()) {
            log.info("No digital release date found for movies");
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(() -> {
            List<Movie> saved = movieService.saveAll(movies);
            log.info("Saved {} movies with digital release dates", saved.size());
            return saved;
        }, executor);
    }

    @Transactional
    private Movie fetchReleaseDateFromTmdbAndUpdate(Movie movie) {
        TmdbReleaseDatesResponse response = tmdbService.getReleaseDatesByMovieId(movie.getId());
        if (response == null || response.getResults().isEmpty()) {
            log.warn("Failed to fetch release dates for movie {}", movie.getTitle());
            return movie;
        }

        List<TmdbCountryRelease> allReleases = response.getResults();

        return movieService.updateUsReleaseDates(movie, allReleases);
    }

    public TmdbMovieDetailsResponse getMovieDetails(Long movieId) {
        return tmdbService.getMovieDetails(movieId);
    }

    private TmdbMovieDetailsResponse safeFetchMovieDetails(Long id) {
        try {
            return tmdbService.getMovieDetails(id);
        } catch (Exception e) {
            log.warn("Failed to fetch details for TMDB ID {}: {}", id, e.getMessage());
            return null;
        }
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

    public void syncYouTubeTrailers() {
        List<Movie> moviesWithoutTrailers = movieService.moviesWithNoTrailers();
        if (moviesWithoutTrailers.isEmpty()) {
            log.info("All Movies have trailers");
            return;
        }

        List<Movie> moviesToUpdate = new ArrayList<>(moviesWithoutTrailers.size());

        moviesWithoutTrailers.parallelStream().forEach(movie -> {
            try {
                Long traktId = movie.getTraktId();
                List<TraktAllVideosResponse> allVideos = traktService.getAllVideos(traktId);

                Optional<TraktAllVideosResponse> trailerResponse = allVideos.stream()
                        .filter(Objects::nonNull)
                        .filter(video -> "Official Trailer".equals(video.getTitle()))
                        .findFirst();

                trailerResponse.ifPresent(trailer -> {
                    movie.setTrailer(trailer.getUrl());
                    moviesToUpdate.add(movie);
                });
            } catch (Exception e) {
                log.error("Failed to sync trailer for movie ID {}: {}", movie.getTraktId(), e.getMessage());
            }
        });

        if (!moviesToUpdate.isEmpty()) {
            movieService.saveAll(moviesToUpdate);
            log.info("Successfully updated trailers for {} movies", moviesToUpdate.size());
        }
    }
}
