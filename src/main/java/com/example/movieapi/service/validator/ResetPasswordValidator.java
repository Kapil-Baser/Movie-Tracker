package com.example.movieapi.service.validator;

import com.example.movieapi.dto.ResetPasswordDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ResetPasswordValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return ResetPasswordValidator.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {

        ResetPasswordDto dto = (ResetPasswordDto) target;

        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            errors.rejectValue("newPassword", "password.mismatch", "Passwords do not match");
        }
    }
}
