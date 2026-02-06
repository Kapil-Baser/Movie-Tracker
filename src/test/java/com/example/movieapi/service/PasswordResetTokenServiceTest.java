package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.PasswordResetToken;
import com.example.movieapi.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    private final String VALID_EMAIL = "vaild@example.com";

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @Test
    void createToken_shouldCreateAndSaveToken_whenUserExists() {
        AppUser appUser = new AppUser();
        when(userService.loadUserByEmail(VALID_EMAIL)).thenReturn(appUser);
        when(passwordResetTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser)).thenReturn(List.of());

        String token = passwordResetTokenService.createToken(VALID_EMAIL);

        assertThat(token).isNotNull();

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());

        PasswordResetToken passwordResetToken = captor.getValue();
        assertThat(passwordResetToken.getTokenHash()).isNotNull();
        assertThat(passwordResetToken.isRevoked()).isFalse();
        assertThat(passwordResetToken.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(passwordResetToken.getUser()).isEqualTo(appUser);

        InOrder inOrder = inOrder(userService, passwordResetTokenRepository);
        inOrder.verify(userService).loadUserByEmail(VALID_EMAIL);
        inOrder.verify(passwordResetTokenRepository).findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser);
        inOrder.verify(passwordResetTokenRepository).saveAll(List.of());
    }

    @Test
    void createToken_shouldRevokeExistingTokens_beforeCreatingNewOne() {
        AppUser appUser = new AppUser();

        PasswordResetToken activeToken = new PasswordResetToken();
        activeToken.setUser(appUser);
        activeToken.setRevoked(false);

        when(userService.loadUserByEmail(VALID_EMAIL)).thenReturn(appUser);

        when(passwordResetTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser)).thenReturn(List.of(activeToken));

        String token = passwordResetTokenService.createToken(VALID_EMAIL);

        assertThat(token).isNotNull();

        assertThat(activeToken.isRevoked()).isTrue();
        assertThat(activeToken.getConfirmedAt()).isNotNull();


        verify(userService).loadUserByEmail(VALID_EMAIL);
        verify(passwordResetTokenRepository).findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser);
        verify(passwordResetTokenRepository).saveAll(List.of(activeToken));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

}