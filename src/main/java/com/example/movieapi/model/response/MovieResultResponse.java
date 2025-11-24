package com.example.movieapi.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class MovieResultResponse {

    @JsonProperty("adult")
    private boolean isAdult;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("genre_ids")
    private Set<Integer> genreIds;

    private Long id;

    @JsonProperty("original_language")
    private String originalLanguage;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("poster_path")
    private String posterPath;

    private String overview;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private String title;
}
