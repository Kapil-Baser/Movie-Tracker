package com.example.movieapi.pagination;

import com.example.movieapi.exception.InvalidCursorException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

class MovieCursorCodecTest {

    MovieCursorCodec movieCursorCodec;
    static ValidatorFactory validatorFactory;

    @BeforeAll
    static void setUpValidatorFactory() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
    }

    @AfterAll
    static void closeValidatorFactory() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();

        Validator validator = validatorFactory.getValidator();

        movieCursorCodec = new MovieCursorCodec(objectMapper, validator);
    }


    @Test
    void validCursorGetsCorrectlyDecodedAndEncoded() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 11, 26, 13, 0, 0);
        MovieCursor originalCursor = new MovieCursor(101L, timestamp);

        String encoded = movieCursorCodec.encode(originalCursor);

        assertThat(encoded)
                .isNotNull()
                .isBase64();

        MovieCursor decodedCursor = movieCursorCodec.decode(encoded);

        assertThat(decodedCursor).isEqualTo(originalCursor);
    }

    @Test
    void decodeThrowsExceptionWhenGivenMalformedBase64String() {
        String malformedBase64String = "%%%invalid-base64%%%";

        assertThatThrownBy(() -> movieCursorCodec.decode(malformedBase64String))
                .isInstanceOf(InvalidCursorException.class)
                .hasMessage("Invalid movie cursor");
    }

}