package com.example.movieapi.service;

import com.example.movieapi.model.MovieResult;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
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
}
