package com.example.movieapi.dto;

public record MovieRuntimeUpdateSummary(int moviesMissingRuntime, int moviesUpdatedWithRuntime) {

    public static MovieRuntimeUpdateSummary empty() {
        return new MovieRuntimeUpdateSummary(0, 0);
    }
}
