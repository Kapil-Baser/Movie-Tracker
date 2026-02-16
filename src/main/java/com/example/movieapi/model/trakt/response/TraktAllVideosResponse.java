package com.example.movieapi.model.trakt.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;

@Data
public class TraktAllVideosResponse {
    private String title;
    private String country;
    private String language;
    private boolean official;
    @JsonProperty("published_at")
    private LocalDateTime publishedAt;
    private String site;
    private String type;
    private int size;
    private String url;
}
