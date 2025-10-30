package com.example.movieapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieByTitleResults {
    @JsonProperty("page")
    private int page;
    @JsonProperty("results")
    private List<Movie> movieResults;
    @JsonProperty("total_pages")
    private int totalPages;
    @JsonProperty("total_results")
    private int totalResults;
}
