package com.example.movieapi.service;

import static org.assertj.core.api.Assertions.*;

import com.example.movieapi.utility.FormatUtil;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;


class MoviePolicyServiceTest {
    private static final String FORMATTED_RELEASE_DATE = FormatUtil.formatReleaseDate(LocalDate.now());

    @Test
    void isMovieSubscribable_returnsTrueWhenGivenDateIsValid() {
        String streamingDate = "Unknown";
        boolean result = MoviePolicyService.isMovieSubscribable(FORMATTED_RELEASE_DATE, streamingDate);
        assertThat(result).isTrue();
    }

    @Test
    void isMovieSubscribable_returnsFalseWhenGivenDateIsInvalid() {
        String streamingDate = "Feb 20, 2026";
        boolean result = MoviePolicyService.isMovieSubscribable(FORMATTED_RELEASE_DATE, streamingDate);
        assertThat(result).isFalse();
    }
}