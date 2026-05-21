package com.example.movieapi.service;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;



class MoviePolicyServiceTest {

    @Test
    void isMovieSubscribable_returnsTrueWhenGivenDateIsValid() {
        String formattedReleaseDate = "May 12, 2026";
        String streamingDate = "Unknown";
        boolean result = MoviePolicyService.isMovieSubscribable(formattedReleaseDate, streamingDate);
        assertThat(result).isTrue();
    }

    @Test
    void isMovieSubscribable_returnsFalseWhenGivenDateIsInvalid() {
        String formattedReleaseDate = "May 12, 2026";
        String streamingDate = "Feb 20, 2026";
        boolean result = MoviePolicyService.isMovieSubscribable(formattedReleaseDate, streamingDate);
        assertThat(result).isFalse();
    }
}