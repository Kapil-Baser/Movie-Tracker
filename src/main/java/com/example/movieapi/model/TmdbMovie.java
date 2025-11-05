package com.example.movieapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovie {

    @JsonProperty("adult")
    private boolean adult;
    @JsonProperty("id")
    private long id;
    @JsonProperty("imdb_id")
    private String imdbId;
    @JsonProperty("original_language")
    private String originalLanguage;
    @JsonProperty("original_title")
    private String originalTitle;
    @JsonProperty("overview")
    private String overview;
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    @JsonProperty("runtime")
    private int runtime;
}
