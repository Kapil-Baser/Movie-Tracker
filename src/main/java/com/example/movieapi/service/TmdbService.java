package com.example.movieapi.service;

import com.example.movieapi.model.response.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class TmdbService {

    private final RestClient restClient;

    public TmdbService(@Qualifier("tmdbServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public TmdbTrendingMoviesResponse getTrendingMoviesByDayOrWeek(String timeWindow) {
        TmdbTrendingMoviesResponse pagedResults = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/trending/movie/" + timeWindow)
                        .build())
                .retrieve()
                .body(TmdbTrendingMoviesResponse.class);

        return Objects.requireNonNull(pagedResults);
    }

    /*
        This method fetches the release dates of a movie by its id.
     */
    @Retryable(
            includes = ResourceAccessException.class,
            maxRetries = 4,
            jitter = 100,
            multiplier = 2,
            maxDelay = 1500
    )
    public TmdbReleaseDatesResponse getReleaseDatesByMovieId(Long movieId) {
        TmdbReleaseDatesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/" + movieId + "/release_dates").build())
                .retrieve()
                .body(TmdbReleaseDatesResponse.class);

        return Objects.requireNonNull(response);
    }

    public TmdbDiscoverResponse getTrendingHorrorMovies() {
        TmdbDiscoverResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("discover/movie")
                        .queryParam("with_original_language", "en")
                        .queryParam("with_genres", "27")
                        .build())
                .retrieve()
                .body(TmdbDiscoverResponse.class);

        return Objects.requireNonNull(response);
    }

    public TmdbConfigurationResponse getConfigurations() {
        TmdbConfigurationResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/configuration").build())
                .retrieve()
                .body(TmdbConfigurationResponse.class);

        return Objects.requireNonNull(response);
    }

    @Retryable(
            includes = ResourceAccessException.class,
            maxRetries = 4,
            jitter = 100,
            multiplier = 2,
            maxDelay = 1500
    )
    public TmdbTrendingMoviesResponse getTrendingMovies(int page) {
        TmdbTrendingMoviesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/now_playing")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbTrendingMoviesResponse.class);
        return Objects.requireNonNull(response);
    }

    public List<MovieResultResponse> getUpcomingMovies(int page) {
        TmdbDiscoverResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("discover/movie")
                        .queryParam("page", page)
                        .queryParam("with_original_language", "en")
                        .queryParam("primary_release_year", LocalDate.now().getYear())
                        .queryParam("primary_release_date.gte", LocalDate.now().withDayOfMonth(1))
                        .queryParam("primary_release_date.lte", LocalDate.now().plusWeeks(12))
                        .build())
                .retrieve()
                .body(TmdbDiscoverResponse.class);

        return Objects.requireNonNull(response).getResults();
    }

    public List<MovieResultResponse> discoverMovies(int releaseYear, LocalDate minReleaseDate, int releaseType) {
        TmdbDiscoverResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("discover/movie")
                        .queryParam("page", 1)
                        .queryParam("with_original_language", "en")
                        //.queryParam("primary_release_year", releaseYear)
                        //.queryParam("primary_release_date.gte", minReleaseDate)
                        //.queryParam("primary_release_date.lte", LocalDate.now().plusMonths(3))
                        .queryParam("with_release_type", "4")
                        .build())
                .retrieve()
                .body(TmdbDiscoverResponse.class);

        return Objects.requireNonNull(response).getResults();
    }

    @Retryable(
            includes = ResourceAccessException.class,
            maxRetries = 4,
            jitter = 100,
            multiplier = 2,
            maxDelay = 1500
    )
    public TmdbMovieDetailsResponse getMovieDetails(Long tmdbId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/" + tmdbId)
                        .queryParam("append_to_response", "release_dates")
                        .build())
                .retrieve()
                .body(TmdbMovieDetailsResponse.class);
    }


    public TmdbMovieDetailsResponse safeGetMovieDetails(Long tmdbId) {
        try {
            return getMovieDetails(tmdbId);
        } catch (Exception e) {
            log.warn("Failed to fetch details for IMDB ID {}: {}", tmdbId, e.getMessage());
            return null;
        }
    }
}
