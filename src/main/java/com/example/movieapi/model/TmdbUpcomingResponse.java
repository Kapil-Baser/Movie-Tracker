package com.example.movieapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TmdbUpcomingResponse {

    private int page;

    @JsonProperty("results")
    private List<MovieResult> movieResults;

    @JsonProperty("total_pages")
    private int totalPages;

    @JsonProperty("total_results")
    private Long totalResults;
}
