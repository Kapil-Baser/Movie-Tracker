package com.example.movieapi.service;

import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.model.trakt.response.TraktMostAnticipatedResponse;
import com.example.movieapi.model.trakt.response.TraktTrendingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class TraktService {

    private final RestClient traktServiceClient;


    public TraktService(@Qualifier("traktServiceClient") RestClient traktServiceClient) {
        this.traktServiceClient = traktServiceClient;
    }

    public TraktMovie safeFetchMovieDetails(String id) {
        try {
            return getExtendedMovieDetails(id);
        } catch (Exception e) {
            log.warn("Failed to fetch details for IMDB ID {}: {}", id, e.getMessage());
            return null;
        }
    }

    public List<TraktTrendingResponse> getTrendingMovies() {
        return traktServiceClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/movies/trending")
                        .queryParam("page", 1 )
                        .queryParam("limit", 20)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TraktTrendingResponse>>() {});
    }

    public String getTrendingMoviesApi() {
        return traktServiceClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/movies/trending")
                        .queryParam("page", 1)
                        .queryParam("limit", 20)
                        .build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public List<TraktMovie> getPopularMovies() {
        return traktServiceClient
                .get()
                .uri(uriBuilder -> uriBuilder.path("/movies/popular").build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TraktMovie>>() {
                });
    }

    public TraktMovie getExtendedMovieDetails(String movieId) {
        return traktServiceClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movies/" + movieId).build())
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(TraktMovie.class);
    }

    public String getReleaseDates(String movieId) {
        return traktServiceClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movies/" + movieId + "/releases").build())
                .retrieve()
                .body(String.class);
    }

    public List<TraktMostAnticipatedResponse> getAnticipated() {
        return traktServiceClient.get()
                .uri("/movies/anticipated")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<TraktMostAnticipatedResponse>>() {
                });
    }
}
