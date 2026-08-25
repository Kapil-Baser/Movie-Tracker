package com.example.movieapi.dto;

import com.example.movieapi.entity.Movie;

public record DigitalReleaseResult(Movie movie, ReleaseDateFetchStatus status) {
}
