package com.example.movieapi.event;

import com.example.movieapi.entity.Movie;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.service.MovieService;
import com.example.movieapi.service.TmdbService;
import com.example.movieapi.service.TraktService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class MovieEnrichmentEventListener {
    private final TraktService traktService;
    private final TmdbService tmdbService;
    private final MovieService movieService;

    @Autowired
    public MovieEnrichmentEventListener(TraktService traktService, TmdbService tmdbService, MovieService movieService) {
        this.traktService = traktService;
        this.tmdbService = tmdbService;
        this.movieService = movieService;
    }


    @Async
    @EventListener(condition = "#event.provider == 'Trakt'")
    public void enrichMoviesFromTrakt(MovieEnrichmentEvent event) {
        List<Movie> moviesToEnrich = event.moviesToEnrich();

        // Get the imdbIds for further calls
        List<String> imdbIds = moviesToEnrich.parallelStream()
                .map(Movie::getImdbId)
                .filter(Objects::nonNull)
                .toList();
        log.info("Requesting details about {} new movies from Trakt API with IMDB IDs: {}", imdbIds.size(), imdbIds);

        // Fetching the movie details
        List<TraktMovie> traktMovies = imdbIds.stream()
                .map(traktService::safeFetchMovieDetails)
                .filter(Objects::nonNull)
                .toList();
        log.info("Fetched {} out of {} movies from Trakt API", traktMovies.size(), imdbIds.size());

        List<Movie> enrichedMovies = movieService.updateTraktMovies(traktMovies, moviesToEnrich);
        log.info("Enriched {} movies from Trakt API", enrichedMovies.size());
    }

    @Async
    @EventListener(condition = "#event.provider == 'TMDB'")
    public void enrichMoviesFromTmdb(MovieEnrichmentEvent event) {
        List<Movie> moviesToEnrich = event.moviesToEnrich();

        // Get the imdbIds for further calls
        List<Long> tmdbIds = moviesToEnrich.parallelStream()
                .map(Movie::getTmdbId)
                .filter(Objects::nonNull)
                .toList();
        log.info("Requesting details about {} new movies from TMDB API with TMDB IDs: {}", tmdbIds.size(), tmdbIds);

        // Fetching the movie details
        List<TmdbMovieDetailsResponse> tmdbMovies = tmdbIds.stream()
                .map(tmdbService::safeGetMovieDetails)
                .filter(Objects::nonNull)
                .toList();
        log.info("Fetched {} out of {} movies from TMDB API", tmdbMovies.size(), tmdbIds.size());

        List<Movie> enrichedMovies = movieService.updateTmdbMovies(tmdbMovies, moviesToEnrich);
        log.info("Enriched {} movies from TMDB API", enrichedMovies.size());
    }
}
