package com.example.movieapi.service;

import com.example.movieapi.utility.FormatUtil;

import java.time.LocalDate;

public class MoviePolicyService {

    private MoviePolicyService() {}

    public static boolean isMovieSubscribable(String formattedReleaseDate, String streamingDate) {

        LocalDate parsedReleaseDate = FormatUtil.parseReleaseDate(formattedReleaseDate);

        if (!isAlreadyReleased(streamingDate)) {
            return checkMovieStreamingEligibility(parsedReleaseDate);
        }
        return false;
    }

    private static boolean checkMovieStreamingEligibility(LocalDate releaseDate) {
        LocalDate cutOffDate = LocalDate.now().minusMonths(3);
        return releaseDate.isAfter(cutOffDate);
    }

    private static boolean isAlreadyReleased(String streamingDate) {
        return !streamingDate.equals("Unknown");
    }
}
