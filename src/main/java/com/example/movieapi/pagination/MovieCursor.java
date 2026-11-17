package com.example.movieapi.pagination;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public record MovieCursor(
        @NotNull
        @Positive
        Long id,

        @NotNull
        LocalDateTime createdAt
) {}
