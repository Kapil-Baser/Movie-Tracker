package com.example.movieapi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TmdbDiscoverResponse {

    private int page;

    private List<MovieResultResponse> results;

    @JsonProperty("total_pages")
    private Long totalPages;

    @JsonProperty("total_results")
    private Long totalResults;
}
