package com.example.movieapi.model.response;

import com.example.movieapi.model.TmdbGenre;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbMovieDetailsResponse {

    @JsonProperty("budget")
    private Long budget;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("original_language")
    private String originalLanguage;

    private String overview;

    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("genres")
    private Set<TmdbGenre> genres;

    private Long id;

    @JsonProperty("imdb_id")
    private String imdbId;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private Long revenue;

    private int runtime;

    // Using append_to_response to also get release dates
    @JsonProperty("release_dates")
    private TmdbReleaseDatesResponse releaseDates;
}
