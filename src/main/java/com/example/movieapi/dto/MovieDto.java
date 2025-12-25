package com.example.movieapi.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record MovieDto(Long id, String title, String overview, Set<String> genres, String backdropPath,
                       String posterPath, String usDigitalReleaseDate, String releaseDate, String runtime,
                       String tagline, String imdbId, String trailer) {
}
