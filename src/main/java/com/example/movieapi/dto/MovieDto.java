package com.example.movieapi.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.Set;

@Builder
public record MovieDto(Long id, String title, String overview, Set<String> genres, BigDecimal rating, Long votes,
                       String posterPath, String usDigitalReleaseDate, String releaseDate, String runtime,
                       String imdbId, String trailer) {
}
