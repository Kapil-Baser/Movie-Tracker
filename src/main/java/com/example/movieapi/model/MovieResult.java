package com.example.movieapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MovieResult {

    private Long id;

    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("original_language")
    private String originalLanguage;

    private String overview;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("genre_ids")
    private Set<Integer> genreIds;
}
