package com.example.movieapi.model.trakt.response;

import com.example.movieapi.model.trakt.model.TraktMovie;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TraktMostWatchedMoviesResponse {
    @JsonProperty("watcher_count")
    private long watcherCount;
    @JsonProperty("play_count")
    private long playCount;
    @JsonProperty("collected_count")
    private long collectedCount;
    private TraktMovie movie;
}
