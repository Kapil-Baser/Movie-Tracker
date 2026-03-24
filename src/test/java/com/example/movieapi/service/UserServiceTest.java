package com.example.movieapi.service;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Provider;
import com.example.movieapi.entity.Role;
import com.example.movieapi.event.UserRegisteredEvent;
import com.example.movieapi.repository.RoleRepository;
import com.example.movieapi.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    static final String USER_EMAIL = "test@example.com";

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void createUser() {
        AppUser user = new AppUser();
        user.setUsername("testUsername");
        user.setPassword("testPassword");
        user.setEmail(USER_EMAIL);
        user.setEnabled(false);
        user.setProvider(Provider.LOCAL);
        user.setProviderId(null);
    }

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // given
        when(userRepository.existsByEmail(USER_EMAIL)).thenReturn(true);

        // when
        boolean result = userService.existsByEmail(USER_EMAIL);

        // then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(USER_EMAIL);
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailDoesNotExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        boolean result = userService.existsByEmail("test");
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail("test");
    }

    @Test
    void existsByEmail_shouldReturnFalse_whenEmailIsNull() {
        when(userRepository.existsByEmail(null)).thenReturn(false);

        boolean result = userService.existsByEmail(null);
        assertThat(result).isFalse();
        verify(userRepository).existsByEmail(null);
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        // given
        String email = "user@example.com";
        AppUser user = new AppUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // when
        AppUser returnedUser = userService.loadUserByEmail(email);

        // then
        assertThat(returnedUser).isEqualTo(user);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_shouldThrowException_whenEmailDoesNotExist() {
        // given
        String email = "user@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.loadUserByEmail(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Unregistered email");
        verify(userRepository).findByEmail(email);
    }

    @Test
    void isValidPassword_returnsTrue_whenPasswordMatches() {
        // given
        String rawPassword = "test1234";
        String encodedPassword = "test1234";
        AppUser appUser = new AppUser();
        appUser.setPassword(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // when
        boolean result = userService.isValidPassword(appUser, rawPassword);

        // then
        assertThat(result).isTrue();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void isValidPassword_returnsFalse_whenPasswordDoesNotMatches() {
        String rawPassword = "test1234";
        String encodedPassword = "encodedtest1234";
        AppUser user = new AppUser();
        user.setPassword(encodedPassword);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean result = userService.isValidPassword(user, rawPassword);

        assertThat(result).isFalse();
        verify(passwordEncoder).matches(rawPassword, encodedPassword);
    }

    @Test
    void registerUser_shouldRegisterUser_whenUserIsValid() {
        // given
        RegisterUserDto registerUserDto = new RegisterUserDto();
        registerUserDto.setUsername("test-user");
        registerUserDto.setPassword("test1234");
        registerUserDto.setEmail("testUser@Example.com");

        when(passwordEncoder.encode(registerUserDto.getPassword())).thenReturn(registerUserDto.getPassword());
        when(roleRepository.findByName("ROLE_USER")).thenReturn(new Role());

        // when
        userService.registerUser(registerUserDto);

        // then
        verify(passwordEncoder).encode(registerUserDto.getPassword());
        verify(roleRepository).findByName("ROLE_USER");
        verify(userRepository).save(any(AppUser.class));
        verify(applicationEventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

}