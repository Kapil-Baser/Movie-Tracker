package com.example.movieapi.service;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;



class MoviePolicyServiceTest {

    @Test
    void isMovieSubscribable_returnsTrueWhenGivenDateIsValid() {
        String formattedReleaseDate = "Dec 12, 2025";
        String streamingDate = "Unknown";
        boolean result = MoviePolicyService.isMovieSubscribable(formattedReleaseDate, streamingDate);
        assertThat(result).isTrue();
    }

    @Test
    void isMovieSubscribable_returnsFalseWhenGivenDateIsInvalid() {
        String formattedReleaseDate = "Dec 12, 2025";
        String streamingDate = "Dec 20, 2025";
        boolean result = MoviePolicyService.isMovieSubscribable(formattedReleaseDate, streamingDate);
        assertThat(result).isFalse();
    }
}