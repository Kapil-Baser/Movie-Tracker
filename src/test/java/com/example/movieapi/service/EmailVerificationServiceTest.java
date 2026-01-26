package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.repository.ConfirmationTokenRepository;
import com.example.movieapi.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    ConfirmationTokenRepository confirmationTokenRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    EmailVerificationService emailVerificationService;

    private static final String VALID_EMAIL = "user@example.com";


    @Test
    void createToken_shouldCreateAndSaveNewToken_whenUserExists() {
        AppUser user = new AppUser();

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(user)).thenReturn(List.of());

        String token = emailVerificationService.createToken(VALID_EMAIL);

        assertThat(token).isNotNull();

        verify(confirmationTokenRepository).saveAll(anyList());

        ArgumentCaptor<ConfirmationToken> confirmationTokenCaptor = ArgumentCaptor.forClass(ConfirmationToken.class);
        verify(confirmationTokenRepository).save(confirmationTokenCaptor.capture());

        ConfirmationToken savedToken = confirmationTokenCaptor.getValue();

        assertThat(savedToken.getTokenHash()).isNotNull();
        assertThat(savedToken.isRevoked()).isFalse();
        assertThat(savedToken.getUser()).isEqualTo(user);
        assertThat(savedToken.getCreatedAt()).isNotNull();
        assertThat(savedToken.getExpiresAt()).isAfter(savedToken.getCreatedAt());
    }

    @Test
    void createToken_shouldThrowException_whenUserDoesNotExist() {
        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> emailVerificationService.createToken(VALID_EMAIL))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("User not found");

        verify(userRepository).findByEmail(VALID_EMAIL);
        verifyNoInteractions(confirmationTokenRepository);
    }

    @Test
    void createToken_shouldRevokeAllExistingActiveTokens_beforeCreatingANewToken() {
        AppUser user = new AppUser();
        List<ConfirmationToken> tokens = new ArrayList<>(List.of(new ConfirmationToken(), new ConfirmationToken()));

        tokens.forEach(t -> {
            t.setRevoked(false);
            t.setUser(user);
        });

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(any())).thenReturn(tokens);

        String  token = emailVerificationService.createToken(VALID_EMAIL);

        assertThat(token).isNotNull();

        verify(userRepository).findByEmail(VALID_EMAIL);
        verify(confirmationTokenRepository).saveAll(tokens);


        for ( ConfirmationToken activeToken : tokens ) {
            assertThat(activeToken.isRevoked()).isTrue();
            assertThat(activeToken.getConfirmedAt()).isNotNull();
        }

        verify(confirmationTokenRepository).save(any(ConfirmationToken.class));

    }

}