package com.example.movieapi.service;

import com.example.movieapi.dto.RegisterUserDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Role;
import com.example.movieapi.event.UserRegisteredEvent;
import com.example.movieapi.repository.RoleRepository;
import com.example.movieapi.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

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

    @Test
    void existsByEmail_shouldReturnTrue_whenEmailExists() {
        // given
        String email = "text@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // when
        boolean result = userService.existsByEmail(email);

        // then
        assertThat(result).isTrue();
        verify(userRepository).existsByEmail(email);
    }

    @Test
    void findByEmail_shouldReturnUser_whenEmailExists() {
        // given
        String email = "user@example.com";
        AppUser appUser = new AppUser("test-user", email, "test1234", true, new Role() );
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(appUser));

        // when
        AppUser returnedUser = userService.loadUserByEmail(email);

        // then
        assertThat(returnedUser).isEqualTo(appUser);
        verify(userRepository).findByEmail(email);
    }

    @Test
    void findByEmail_shouldThrowException_whenEmailDoesNotExist() {
        // given
        String email = "user@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // when
        AppUser returnedUser = userService.loadUserByEmail(email);

        // then
        assertThat(returnedUser).isNull();
        assertThatThrownBy(() -> userService.loadUserByEmail(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("Unregistered email");
        verify(userRepository).findByEmail(email);
    }

    @Test
    void isValidPassword_returnsTrue_whenPasswordIsValid() {
        // given
        String rawPassword = "test1234";
        String encodedPassword = "test1234";
        AppUser appUser = new AppUser("test-user", "user@example.com", encodedPassword, true, new Role() );
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        // when
        boolean result = userService.isValidPassword(appUser, rawPassword);

        // then
        assertThat(result).isTrue();
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