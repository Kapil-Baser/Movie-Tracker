package com.example.movieapi.pagination;

import com.example.movieapi.exception.InvalidCursorException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

@Component
public class MovieCursorCodec {

    private final ObjectMapper objectMapper;
    private final Validator validator;

    public MovieCursorCodec(ObjectMapper objectMapper, Validator validator) {
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    public MovieCursor toMovieCursor(KeysetScrollPosition scrollPosition) {
        var keys = scrollPosition.getKeys();

        return new MovieCursor(
                (Long) keys.get("id"),
                (LocalDateTime) keys.get("createdAt")
        );
    }

    public ScrollPosition toScrollPosition(MovieCursor cursor) {
        Map<String, Object> keys = Map.of(
                "id", cursor.id(),
                "createdAt", cursor.createdAt()
        );

        return ScrollPosition.forward(keys);
    }

    public String encode(MovieCursor cursor) {
        try {
            byte[] jsonBytes = objectMapper.writeValueAsBytes(cursor);

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(jsonBytes);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Could not encode movie cursor", e);
        }
    }

    public MovieCursor decode(String encodedCursor) {
        try {
            byte[] decodedBytes = Base64.getUrlDecoder()
                    .decode(encodedCursor);

            MovieCursor cursor = objectMapper.readValue(decodedBytes, MovieCursor.class);
            if(!isValidCursor(cursor)) {
                throw new InvalidCursorException("Invalid cursor data");
            }
            return cursor;

        } catch (IllegalArgumentException | JacksonException e) {
            throw new InvalidCursorException("Invalid movie cursor", e);
        }
    }

    private boolean isValidCursor(MovieCursor cursor) {
        Set<ConstraintViolation<MovieCursor>> violations = validator.validate(cursor);
        return violations.isEmpty();
    }
}
