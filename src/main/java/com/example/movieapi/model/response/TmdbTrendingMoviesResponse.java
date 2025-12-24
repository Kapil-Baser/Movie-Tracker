package com.example.movieapi.model.response;

import com.example.movieapi.model.tmdb.model.TmdbMovie;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbTrendingMoviesResponse {
    private int page;

    private List<TmdbMovie> results;

    @JsonProperty("total_pages")
    private Long totalPages;

    @JsonProperty("total_results")
    private Long totalResults;
}
