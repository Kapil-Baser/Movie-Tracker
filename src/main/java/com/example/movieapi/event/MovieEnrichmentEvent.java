package com.example.movieapi.event;

import com.example.movieapi.entity.Movie;

import java.util.List;

public record MovieEnrichmentEvent (List<Movie> moviesToEnrich) { }
