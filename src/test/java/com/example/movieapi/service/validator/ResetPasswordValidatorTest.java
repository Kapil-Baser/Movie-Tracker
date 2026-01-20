package com.example.movieapi.service.validator;

import com.example.movieapi.dto.ResetPasswordDto;
import com.example.movieapi.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.*;

class ResetPasswordValidatorTest {

    private final ResetPasswordValidator validator = new ResetPasswordValidator();

    @Test
    void supports_returnsTrue_ForValidClass() {
        boolean result = validator.supports(ResetPasswordValidator.class);

        assertThat(result).isTrue();
    }

    @Test
    void supports_returnsFalse_ForInvalidClass() {
        boolean result = validator.supports(UserService.class);

        assertThat(result).isFalse();
    }

    @Test
    void validate_ShouldNotAddErrors_WhenPasswordsMatch() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setNewPassword("12345");
        dto.setConfirmNewPassword("12345");

        Errors errors = new BeanPropertyBindingResult(dto, "dto");

        validator.validate(dto, errors);

        assertThat(errors.hasErrors()).isFalse();
    }

    @Test
    void validate_SholdRejectWithCode_PasswordMismatch() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setNewPassword("12345");
        dto.setConfirmNewPassword("1234");

        Errors errors = new BeanPropertyBindingResult(dto, "dto");

        validator.validate(dto, errors);

        String errorCode = errors.getFieldError("newPassword").getCode();

        assertThat(errorCode).isEqualTo("password.mismatch");
    }

    @Test
    void validate_SholdRejectWithCode_Invalid_OnNullPassword() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setNewPassword(null);
        dto.setConfirmNewPassword("12345");

        Errors errors = new BeanPropertyBindingResult(dto, "dto");

        validator.validate(dto, errors);

        String errorCode = errors.getFieldError("newPassword").getCode();

        assertThat(errorCode).isEqualTo("invalid.password");
    }

    @Test
    void validate_SholdRejectWithCode_Invalid_OnEmptyPassword() {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setNewPassword("12345");
        dto.setConfirmNewPassword("");

        Errors errors = new BeanPropertyBindingResult(dto, "dto");

        validator.validate(dto, errors);

        String errorCode = errors.getFieldError("confirmNewPassword").getCode();

        assertThat(errorCode).isEqualTo("invalid.password");
    }

}