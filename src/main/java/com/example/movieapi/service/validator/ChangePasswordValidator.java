package com.example.movieapi.service.validator;

import com.example.movieapi.dto.ChangePasswordDto;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ChangePasswordValidator implements Validator {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ChangePasswordValidator(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return ChangePasswordDto.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        ChangePasswordDto dto = (ChangePasswordDto) target;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuthenticatedUser user = (AuthenticatedUser) userService.loadUserByUsername(auth.getName());

        // Checking if new passwords match
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            errors.rejectValue("newPassword", "password.mismatch", "Passwords do not match");
        }

        // Checking if old password is correct
        if(!userService.isValidPassword(user.getUser(), dto.getCurrentPassword())) {
            errors.rejectValue("currentPassword", "password.incorrect", "Current password is incorrect");
        }

        // Checking if new password is different from current password
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            errors.rejectValue("invalidPassword", "password.invalid", "New password cannot be same as current password");
        }
    }
}
