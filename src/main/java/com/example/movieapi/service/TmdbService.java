package com.example.movieapi.service;

import com.example.movieapi.model.MovieResult;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
import com.example.movieapi.model.TmdbUpcomingResponse;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.model.response.TmdbConfigurationResponse;
import com.example.movieapi.model.response.TmdbDiscoverResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TmdbService {

    private final RestClient restClient;

    public TmdbService(RestClient restClient) {
        this.restClient = restClient;
    }

/*    public List<MovieResultResponse> getUpcomingMovies() {
        TmdbUpcomingResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/movie/upcoming").build())
                .retrieve()
                .body(TmdbUpcomingResponse.class);

        return Objects.requireNonNull(response).getMovieResults();
    }*/

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

    public List<MovieResultResponse> getUpcomingMovies() {
        TmdbDiscoverResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("discover/movie")
                        .queryParam("with_original_language", "en")
                        .queryParam("primary_release_year", LocalDate.now().getYear())
                        .queryParam("primary_release_date.gte", LocalDate.now().withDayOfMonth(1))
                        .queryParam("primary_release_date.lte", LocalDate.now().plusWeeks(3))
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
}
