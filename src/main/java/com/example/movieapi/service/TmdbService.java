package com.example.movieapi.service;

import com.example.movieapi.model.MovieResult;
import com.example.movieapi.model.TmdbUpcomingResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;

@Service
public class TmdbService {

    private final RestClient restClient;

    public TmdbService(RestClient restClient) {
        this.restClient = restClient;
    }

    public List<MovieResult> getUpcomingMovies() {
        TmdbUpcomingResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/upcoming").build())
                .retrieve()
                .body(TmdbUpcomingResponse.class);

        return Objects.requireNonNull(response).getMovieResults();
    }
}
