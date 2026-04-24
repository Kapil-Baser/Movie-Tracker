package com.example.movieapi.model.response;

import com.example.movieapi.model.tmdb.model.TmdbGenre;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class TmdbGenreResponse {

    @JsonProperty("genres")
    List<TmdbGenre> tmdbGenres;
}
