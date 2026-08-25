package com.example.movieapi.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DigitalReleaseSummary(long moviesScanned,
                                    long releaseDateFound,
                                    List<Long> foundMovieIds,
                                    long releaseDateNotFound,
                                    List<Long> notFoundMovieIds,
                                    long failures) {
    public static DigitalReleaseSummary empty() {
        return new DigitalReleaseSummary(0, 0, List.of(), 0, List.of(), 0);
    }
}
