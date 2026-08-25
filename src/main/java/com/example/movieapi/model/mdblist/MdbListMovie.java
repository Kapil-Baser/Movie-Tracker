package com.example.movieapi.model.mdblist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdbListMovie {
    @JsonProperty("id")
    private Long tmdbId;
    @JsonProperty("mediatype")
    private String mediaType;
    @JsonProperty("imdb_id")
    private String imdbId;
    @JsonProperty("tvdb_id")
    private String tvdbId;
    private MdbListMovieIds ids;
    private String title;
    private Integer year;
    private LocalDate released;
    @JsonProperty("released_digital")
    private LocalDate releasedDigital;
    private String language;
    @JsonProperty("spoken_language")
    private String spokenLanguage;
    private String country;
    @JsonProperty("release_year")
    private String releaseYear;
    @JsonProperty("release_date")
    private LocalDate releaseDate;
    private String status;
    private Integer runtime;
    private List<MdbListMovieRating> ratings;
    private String rank;
    private String certification;
    private String trailer;
}
