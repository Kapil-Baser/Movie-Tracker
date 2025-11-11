package com.example.movieapi.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public record MovieDto(String title, String overview, Set<String> genres, String backdropPath,
                       String posterPath, String usDigitalReleaseDate, String releaseDate) {
}
