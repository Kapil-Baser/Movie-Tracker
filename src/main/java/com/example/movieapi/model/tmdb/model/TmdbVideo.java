package com.example.movieapi.model.tmdb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbVideo {

    @JsonProperty("iso_3166_1")
    private String countryCode;

    private String name;

    private String key;

    private String site;

    private String size;

    private String type;

    private boolean official;

    private String id;

}
