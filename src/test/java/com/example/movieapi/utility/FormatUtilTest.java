package com.example.movieapi.utility;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class FormatUtilTest {

    @Test
    void formatRuntime_shouldFormatRuntimeCorrectly() {
        int runtime = 145;
        String result = FormatUtil.formatRuntime(runtime);
        assertThat(result).isEqualTo("2h 25min");
    }

    @ParameterizedTest
    @CsvSource({
            "0, 0h 0min",
            "59, 0h 59min",
            "60, 1h 0min",
    })
    void formatRuntime_shouldHandleVariousInputs(int runtime, String expected) {
        assertThat(FormatUtil.formatRuntime(runtime)).isEqualTo(expected);
    }

    @Test
    void formatReleaseDate_shouldFormatReleaseDateCorrectly() {
        LocalDate date = LocalDate.of(2025, 12, 30);
        String result = FormatUtil.formatReleaseDate(date);
        assertThat(result).isEqualTo("Dec 30, 2025");
    }

    @ParameterizedTest
    @CsvSource({
            "2024-10-03, 'Oct 3, 2024'",
            "2025-02-03, 'Feb 3, 2025'",
            "2022-03-07, 'Mar 7, 2022'",
    })
    void formatReleaseDate_shouldFormatVariousReleaseDateCorrectly(String dateAsString, String expected) {
        LocalDate date = LocalDate.parse(dateAsString);
        assertThat(FormatUtil.formatReleaseDate(date)).isEqualTo(expected);
    }

    @Test
    void formatReleaseDate_shouldReturnUnknownWhenDateIsNull() {
        String result = FormatUtil.formatReleaseDate(null);
        assertThat(result).isEqualTo("Unknown");
    }
}