package com.example.movieapi.service;

import com.example.movieapi.model.PagedResults;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@Slf4j
public class TmdbService {

    private final RestClient restClient;

    public TmdbService(@Qualifier("tmdbServiceClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public PagedResults getTrendingMoviesByDayOrWeek(String timeWindow) {
        PagedResults pagedResults = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/trending/movie/" + timeWindow)
                        .build())
                .retrieve()
                .body(PagedResults.class);

        return Objects.requireNonNull(pagedResults);
    }

    /*
        This method fetches the release dates of a movie by its id.
     */
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

    public TmdbTrendingMoviesResponse getTrendingMovies(int page) {
        TmdbTrendingMoviesResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/now_playing")
                        .queryParam("page", page)
                        .build())
                .retrieve()
                .body(TmdbTrendingMoviesResponse.class);
        return Objects.requireNonNull(response);
    }

    public List<TmdbMovieDetailsResponse> getTrendingMoviesAsync(List<Long> tmdbIds, Executor movieExecutor) {
        List<CompletableFuture<TmdbMovieDetailsResponse>> futures = tmdbIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> getMovieDetails(id), movieExecutor)
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

    public List<MovieResultResponse> getUpcomingMovies() {
        TmdbDiscoverResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("discover/movie")
                        .queryParam("with_original_language", "en")
                        .queryParam("primary_release_year", LocalDate.now().getYear())
                        .queryParam("primary_release_date.gte", LocalDate.now().withDayOfMonth(1))
                        .queryParam("primary_release_date.lte", LocalDate.now().plusWeeks(4))
                        .build())
                .retrieve()
                .body(TmdbDiscoverResponse.class);

        return Objects.requireNonNull(response).getResults();
    }

    public List<MovieResultResponse> discoverMovies(int releaseYear, LocalDate minReleaseDate, int releaseType) {
        TmdbDiscoverResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("discover/movie")
                        .queryParam("with_original_language", "en")
                        .queryParam("primary_release_year", releaseYear)
                        .queryParam("release_date.gte", minReleaseDate)
                        .queryParam("release_date.lte", LocalDate.now())
                        .queryParam("with_release_type", releaseType)
                        .build())
                .retrieve()
                .body(TmdbDiscoverResponse.class);

        return Objects.requireNonNull(response).getResults();
    }

    public TmdbMovieDetailsResponse getMovieDetails(Long tmdbId) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/" + tmdbId)
                        .queryParam("append_to_response", "release_dates")
                        .build())
                .retrieve()
                .body(TmdbMovieDetailsResponse.class);
    }
}
