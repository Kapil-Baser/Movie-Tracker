package com.example.movieapi.event;

import com.example.movieapi.entity.Movie;

public record MovieReleasedEvent(Movie movie) {
}
