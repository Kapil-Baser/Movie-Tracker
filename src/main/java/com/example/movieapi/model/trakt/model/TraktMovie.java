package com.example.movieapi.model.trakt.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktMovie {

    private String title;
    private int year;
    private TraktIds ids;
    @JsonProperty("tagline")
    private String tagLine;
    private String overview;
    private int runtime;
    private String country;
    private String trailer;
    @JsonProperty("homepage")
    private String homePage;
    private String status;
    private BigDecimal rating;
    private Long votes;
    private String language;
    private String[] genres;
    @JsonProperty("original_title")
    private String originalTitle;
    private LocalDate released;
    private String certification;
    @JsonProperty("after_credits")
    private boolean hasAfterCredits;
    @JsonProperty("during_credits")
    private boolean hasDuringCredits;
}
