package com.example.movieapi.model.response;

import com.example.movieapi.model.tmdb.model.TmdbCountryRelease;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbReleaseDatesResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("results")
    private List<TmdbCountryRelease> results;
}
