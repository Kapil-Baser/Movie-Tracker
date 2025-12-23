package com.example.movieapi.model.trakt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktIds {
    private String trakt;
    private String slug;
    private String imdb;
    private Long tmdb;
}
