package com.example.movieapi.model.trakt.response;

import com.example.movieapi.model.trakt.model.TraktMovie;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TraktMostAnticipatedResponse {
    @JsonProperty("list_count")
    private int listCount;
    private TraktMovie movie;
}
