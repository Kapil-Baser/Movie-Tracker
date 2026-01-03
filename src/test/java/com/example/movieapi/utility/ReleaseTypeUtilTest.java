package com.example.movieapi.utility;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ReleaseTypeUtilTest {

    @Test
    void getReleaseTypeName_returnsDigitalWhenGivenReleaseType_4() {
        int releaseType = 4;
        String result = ReleaseTypeUtil.getReleaseTypeName(releaseType);
        assertThat(result).isEqualTo("Digital");
    }

    @ParameterizedTest
    @CsvSource({
            "1, 'Premiere'",
            "2, 'Theatrical (Limited)'",
            "3, 'Theatrical'",
            "4, 'Digital'",
            "5, 'Physical'",
            "6, 'TV'",
            "8, 'Unknown'",
    })
    void getReleaseTypeName_returnsCorrectReleaseNameForVariousReleaseTypes(int releaseType, String releaseName) {
        assertThat(ReleaseTypeUtil.getReleaseTypeName(releaseType)).isEqualTo(releaseName);
    }

    @Test
    void isTheatrical_returnsTrueForTheatricalRelease() {
        int releaseType = 3;
        boolean result = ReleaseTypeUtil.isTheatrical(releaseType);
        assertThat(result).isTrue();
    }

    @Test
    void isTheatrical_returnsFalseForNonTheatricalRelease() {
        int releaseType = 5;
        boolean result = ReleaseTypeUtil.isTheatrical(releaseType);
        assertThat(result).isFalse();
    }

    @Test
    void isTheatrical_returnsFalseForInvalidRelease() {
        int releaseType = 0;
        boolean result = ReleaseTypeUtil.isTheatrical(releaseType);
        assertThat(result).isFalse();
    }
}