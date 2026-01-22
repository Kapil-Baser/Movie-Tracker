package com.example.movieapi.service.validator;

import com.example.movieapi.dto.ChangePasswordDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChangePasswordValidatorTest {

    @Nested
    class SupportsTestMethods {
        @Mock
        private UserService userService;
        @Mock
        private PasswordEncoder passwordEncoder;
        @InjectMocks
        private ChangePasswordValidator changePasswordValidator;

        @Test
        void supports_ReturnsTrue_WhenClassIsSupported() {

            boolean result = changePasswordValidator.supports(ChangePasswordDto.class);
            assertThat(result).isTrue();
        }

        @Test
        void supports_ReturnsFalse_WhenClassIsNotSupported() {
            boolean result = changePasswordValidator.supports(String.class);
            assertThat(result).isFalse();
        }
    }

    @Nested
    class ValidateTestMethods {
        @Mock
        private UserService userService;
        @Mock
        private PasswordEncoder passwordEncoder;
        @Mock
        private SecurityContext securityContext;
        @Mock
        private Authentication auth;
        @Mock
        private AuthenticatedUser authenticatedUser;

        @InjectMocks
        private ChangePasswordValidator changePasswordValidator;

        @BeforeEach
        void setup() {
            SecurityContextHolder.setContext(securityContext);
            when(securityContext.getAuthentication()).thenReturn(auth);
            when(auth.getName()).thenReturn("testUser");
            when(userService.loadUserByUsername("testUser")).thenReturn(authenticatedUser);
        }

        @AfterEach
        void tearDown() {
            SecurityContextHolder.clearContext();
        }



        @Test
        void validate_ShouldAccept_WhenPasswordsMatch() {
            ChangePasswordDto changePasswordDto = new ChangePasswordDto();
            changePasswordDto.setNewPassword("new1234");
            changePasswordDto.setConfirmNewPassword("new1234");
            changePasswordDto.setCurrentPassword("oldPassword");

            when(userService.isValidPassword(null, "oldPassword")).thenReturn(true);
            when(passwordEncoder.matches("new1234", null)).thenReturn(false);

            Errors errors = new BeanPropertyBindingResult(changePasswordDto, "changePasswordDto");

            changePasswordValidator.validate(changePasswordDto, errors);

            assertThat(errors.hasErrors()).isFalse();
            verify(userService, times(1)).isValidPassword(null, "oldPassword");
            verify(passwordEncoder, times(1)).matches("new1234", null);
        }

        @Test
        void validate_ShouldReject_WhenPasswordsDoNotMatch() {
            ChangePasswordDto changePasswordDto = new ChangePasswordDto();
            changePasswordDto.setNewPassword("new1234");
            changePasswordDto.setConfirmNewPassword("new4321");
            changePasswordDto.setCurrentPassword("oldPassword");

            when(userService.isValidPassword(null, "oldPassword")).thenReturn(true);
            when(passwordEncoder.matches("new1234", null)).thenReturn(false);

            Errors errors = new BeanPropertyBindingResult(changePasswordDto, "changePasswordDto");


            changePasswordValidator.validate(changePasswordDto, errors);
            String errorCode = errors.getFieldError("newPassword").getCode();

            assertThat(errors.hasErrors()).isTrue();
            assertThat(errorCode).isEqualTo("password.mismatch");
            verify(userService, times(1)).isValidPassword(null, "oldPassword");
            verify(passwordEncoder, times(1)).matches("new1234", null);
        }

        @Test
        void validate_ShouldReject_WhenPasswordsDoNotMatchOldPassword() {
            ChangePasswordDto changePasswordDto = new ChangePasswordDto();
            changePasswordDto.setNewPassword("new1234");
            changePasswordDto.setConfirmNewPassword("new1234");
            changePasswordDto.setCurrentPassword("incorrectOldPassword");

            when(userService.isValidPassword(null, "incorrectOldPassword")).thenReturn(false);
            when(passwordEncoder.matches("new1234", null)).thenReturn(false);

            Errors errors = new BeanPropertyBindingResult(changePasswordDto, "changePasswordDto");
            changePasswordValidator.validate(changePasswordDto, errors);

            String errorCode = errors.getFieldError("currentPassword").getCode();

            assertThat(errors.hasErrors()).isTrue();
            assertThat(errorCode).isEqualTo("password.incorrect");
            verify(userService, times(1)).isValidPassword(null, "incorrectOldPassword");
            verify(passwordEncoder, times(1)).matches("new1234", null);
        }

        @Test
        void validate_ShouldReject_WhenNewPasswordIsSameAsOldPassword() {
            ChangePasswordDto changePasswordDto = new ChangePasswordDto();
            changePasswordDto.setNewPassword("oldPassword");
            changePasswordDto.setConfirmNewPassword("oldPassword");
            changePasswordDto.setCurrentPassword("oldPassword");

            when(userService.isValidPassword(null, changePasswordDto.getCurrentPassword())).thenReturn(true);
            when(passwordEncoder.matches("oldPassword", null)).thenReturn(true);

            Errors errors = new BeanPropertyBindingResult(changePasswordDto, "changePasswordDto");

            changePasswordValidator.validate(changePasswordDto, errors);

            String errorCode = errors.getFieldError("currentPassword").getCode();

            assertThat(errors.hasErrors()).isTrue();
            assertThat(errorCode).isEqualTo("password.invalid");
            verify(userService, times(1)).isValidPassword(null, changePasswordDto.getCurrentPassword());
            verify(passwordEncoder, times(1)).matches("oldPassword", null);
        }
    }



}