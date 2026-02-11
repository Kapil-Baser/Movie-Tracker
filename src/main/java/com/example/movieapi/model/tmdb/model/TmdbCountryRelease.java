package com.example.movieapi.model.tmdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbCountryRelease {

    @JsonProperty("iso_3166_1")
    private String countryCode;

    @JsonProperty("release_dates")
    private List<TmdbReleaseDate> releaseDates;
}
