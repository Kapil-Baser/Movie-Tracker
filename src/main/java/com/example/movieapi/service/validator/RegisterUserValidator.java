package com.example.movieapi.service.validator;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
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
        if (userService.existsByEmail(userDto.getEmail())) {
            errors.rejectValue("email", "email.exists", "An account with this email already exists");
        }
    }
}
