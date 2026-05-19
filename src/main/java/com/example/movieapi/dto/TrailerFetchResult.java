package com.example.movieapi.dto;

import com.example.movieapi.entity.Movie;

public record TrailerFetchResult(Movie movie, TrailerFetchStatus status) {
}
