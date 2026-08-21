package com.example.movieapi.model.response;

import com.example.movieapi.model.tmdb.model.TmdbVideo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmdbVideosResponse {

    List<TmdbVideo> results;
}
