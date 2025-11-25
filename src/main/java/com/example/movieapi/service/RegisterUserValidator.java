package com.example.movieapi.service;

import com.example.movieapi.dto.RegisterUserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Service
@RequiredArgsConstructor
public class RegisterUserValidator implements Validator {

    private final UserService userService;

    @Override
    public boolean supports(Class<?> clazz) {
        return RegisterUserDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        RegisterUserDto userDto = (RegisterUserDto) target;

        // Checking for password match
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            errors.rejectValue("confirmPassword", "password.mismatch", "Password do not match");
        }

        // Checking for email
        if (userService.loadUserByEmail(userDto.getEmail()).isPresent()) {
            errors.rejectValue("email", "email.exists", "An account with this email already exists");
        }
    }
}
