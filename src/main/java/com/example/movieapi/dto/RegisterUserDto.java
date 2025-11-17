package com.example.movieapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserDto {

    @NotEmpty(message = "Username is required")
    @Size(min = 4, max = 50, message = "Username must be between 4 and 50 characters")
    private String username;

    @NotEmpty(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotEmpty(message = "Password is required")
    @Size(min = 4, message = "Password must be at least 4 characters")
    private String password;

    @NotEmpty(message = "Password confirmation is required")
    private String confirmPassword;
}
