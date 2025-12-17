package com.example.movieapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record ForgetPasswordDto(
        @NotEmpty(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        String email
) { }
