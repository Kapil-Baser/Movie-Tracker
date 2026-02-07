package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.PasswordResetToken;
import com.example.movieapi.exception.InvalidTokenException;
import com.example.movieapi.repository.PasswordResetTokenRepository;
import com.example.movieapi.utility.TokenHashUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetTokenServiceTest {

    private final String email = "vaild@example.com";

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordResetTokenService passwordResetTokenService;

    @Test
    void createToken_shouldCreateAndSaveToken_whenUserExists() {
        AppUser appUser = new AppUser();
        when(userService.loadUserByEmail(email)).thenReturn(appUser);
        when(passwordResetTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser)).thenReturn(List.of());

        String token = passwordResetTokenService.createToken(email);

        assertThat(token).isNotNull();

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        verify(passwordResetTokenRepository).save(captor.capture());

        PasswordResetToken passwordResetToken = captor.getValue();
        assertThat(passwordResetToken.getTokenHash()).isNotNull();
        assertThat(passwordResetToken.isRevoked()).isFalse();
        assertThat(passwordResetToken.getExpiresAt()).isAfter(LocalDateTime.now());
        assertThat(passwordResetToken.getUser()).isEqualTo(appUser);

        InOrder inOrder = inOrder(userService, passwordResetTokenRepository);
        inOrder.verify(userService).loadUserByEmail(email);
        inOrder.verify(passwordResetTokenRepository).findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser);
        inOrder.verify(passwordResetTokenRepository).saveAll(List.of());
    }

    @Test
    void createToken_shouldRevokeExistingTokens_beforeCreatingNewOne() {
        AppUser appUser = new AppUser();

        PasswordResetToken activeToken = new PasswordResetToken();
        activeToken.setUser(appUser);
        activeToken.setRevoked(false);

        when(userService.loadUserByEmail(email)).thenReturn(appUser);

        when(passwordResetTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser)).thenReturn(List.of(activeToken));

        String token = passwordResetTokenService.createToken(email);

        assertThat(token).isNotNull();

        assertThat(activeToken.isRevoked()).isTrue();
        assertThat(activeToken.getConfirmedAt()).isNotNull();


        verify(userService).loadUserByEmail(email);
        verify(passwordResetTokenRepository).findAllByUserAndConfirmedAtIsNullAndRevokedFalse(appUser);
        verify(passwordResetTokenRepository).saveAll(List.of(activeToken));
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
    }

    @Test
    void createToken_shouldReturnNull_whenUserDoesNotExist() {
        when(userService.loadUserByEmail(email)).thenThrow(new UsernameNotFoundException("User not found"));

        String token = passwordResetTokenService.createToken(email);

        assertThat(token).isNull();

        verifyNoInteractions(passwordResetTokenRepository);
    }

    @Test
    void validateToken_shouldConfirmAndRevokeToken_whenTokenIsValid() {
        String rawToken = "rawToken";
        String hashedToken = TokenHashUtil.getHashedToken(rawToken);
        AppUser appUser = new AppUser();
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(appUser);
        resetToken.setTokenHash(hashedToken);
        resetToken.setRevoked(false);
        resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(1));

        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(resetToken));

        passwordResetTokenService.validateToken(rawToken);

        assertThat(resetToken.isRevoked()).isTrue();
        assertThat(resetToken.getConfirmedAt()).isNotNull();

        verify(passwordResetTokenRepository).findByTokenHash(hashedToken);
        verify(passwordResetTokenRepository).save(resetToken);
    }

    @Test
    void validateToken_shouldThrowInvalidTokenException_whenTokenDoesNotExist() {
        String rawToken = "invalidToken";
        String hashedToken = TokenHashUtil.getHashedToken(rawToken);

        when(passwordResetTokenRepository.findByTokenHash(hashedToken)).thenThrow(new InvalidTokenException("Invalid Token"));


        assertThatThrownBy(() -> passwordResetTokenRepository.findByTokenHash(hashedToken))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("Invalid Token");

        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
    }

}