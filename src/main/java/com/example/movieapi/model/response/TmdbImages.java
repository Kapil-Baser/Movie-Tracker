package com.example.movieapi.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbImages {

    @JsonProperty("base_url")
    private String baseUrl;

    @JsonProperty("secure_base_url")
    private String secureBaseUrl;
}
