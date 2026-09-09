package com.example.movieapi.dto;

import java.util.List;

public record UpdateMovieRatingSummary(long moviesMissingRating,
                                       long ratingFound,
                                       List<Long> foundMovieIds,
                                       long ratingNotFound,
                                       List<Long> notFoundMovieIds,
                                       long failures) {
    public static UpdateMovieRatingSummary empty() {
        return new UpdateMovieRatingSummary(0, 0, List.of(), 0, List.of(), 0);
    }
}
