package com.example.movieapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbReleaseDate {

    @JsonProperty("certification")
    private String certification;

    @JsonProperty("iso_639_1")
    private String languageCode;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("note")
    private String note;
}
