package com.example.movieapi.model;

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
