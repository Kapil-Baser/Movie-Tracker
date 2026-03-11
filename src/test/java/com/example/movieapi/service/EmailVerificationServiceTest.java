package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.exception.ExpiredTokenException;
import com.example.movieapi.exception.InvalidTokenException;
import com.example.movieapi.exception.UserAlreadyVerifiedException;
import com.example.movieapi.repository.ConfirmationTokenRepository;
import com.example.movieapi.repository.UserRepository;
import com.example.movieapi.utility.TokenHashUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailVerificationServiceTest {

    @Mock
    ConfirmationTokenRepository confirmationTokenRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    EmailVerificationService emailVerificationService;

    @Captor
    ArgumentCaptor<ConfirmationToken> confirmationTokenCaptor;

    static final String VALID_EMAIL = "user@example.com";

    AppUser user = new AppUser();


    @Test
    void createToken_shouldCreateAndSaveNewToken_whenUserExists() {

        when(userRepository.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(user));
        when(confirmationTokenRepository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(user)).thenReturn(List.of());

        String token = emailVerificationService.createToken(VALID_EMAIL);

        assertThat(token).isNotNull();

        verify(confirmationTokenRepository).saveAll(anyList());

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

    @Test
    void confirmAndEnable_shouldConfirmAndEnable_whenTokenIsValidAndNotAlreadyConfirmed() {
        String rawToken = "rawToken";
        String hashedToken = TokenHashUtil.getHashedToken(rawToken);
        user.setEnabled(false);

        ConfirmationToken token = new ConfirmationToken();
        token.setRevoked(false);
        token.setUser(user);
        token.setTokenHash(hashedToken);
        token.setConfirmedAt(null);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(confirmationTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(token));

        emailVerificationService.confirmAndEnable(rawToken);

        InOrder inOrder = inOrder(confirmationTokenRepository, userRepository);

        inOrder.verify(confirmationTokenRepository).findByTokenHash(hashedToken);
        inOrder.verify(confirmationTokenRepository).save(token);
        inOrder.verify(userRepository).save(user);

        assertThat(token.isRevoked()).isTrue();
        assertThat(token.getConfirmedAt()).isNotNull();

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void confirmAndEnable_throwsException_whenTokenIsInvalid() {
        String rawInvalid = "invalidToken";
        String  hashedInvalid = TokenHashUtil.getHashedToken(rawInvalid);

        when(confirmationTokenRepository.findByTokenHash(hashedInvalid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> emailVerificationService.confirmAndEnable(rawInvalid))
                .isInstanceOf(InvalidTokenException.class)
                .hasMessage("This token is invalid");

        verify(confirmationTokenRepository).findByTokenHash(hashedInvalid);
        verifyNoInteractions(userRepository);
    }

    @Test
    void confirmAndEnable_shouldThrowException_whenTokenIsAlreadyConfirmed() {
        String rawToken = "rawToken";
        String hashedToken = TokenHashUtil.getHashedToken(rawToken);

        ConfirmationToken token = new ConfirmationToken();
        token.setUser(user);
        token.setRevoked(true);
        token.setConfirmedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        when(confirmationTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> emailVerificationService.confirmAndEnable(rawToken))
                .isInstanceOf(UserAlreadyVerifiedException.class)
                .hasMessage("Your email is already confirmed");

        verify(confirmationTokenRepository).findByTokenHash(hashedToken);
        verify(confirmationTokenRepository, never()).save(any(ConfirmationToken.class));
        verifyNoInteractions(userRepository);
    }

    @Test
    void confirmAndEnable_shouldThrowException_whenTokenIsExpired() {
        String rawToken = "rawToken";
        String hashedToken = TokenHashUtil.getHashedToken(rawToken);

        ConfirmationToken token = new ConfirmationToken();
        token.setUser(user);
        token.setRevoked(false);
        token.setConfirmedAt(null);
        token.setExpiresAt(LocalDateTime.now().minusMinutes(5));

        when(confirmationTokenRepository.findByTokenHash(hashedToken)).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> emailVerificationService.confirmAndEnable(rawToken))
                .isInstanceOf(ExpiredTokenException.class)
                .hasMessage("This token has expired");

        verify(confirmationTokenRepository).findByTokenHash(hashedToken);
        verify(confirmationTokenRepository, never()).save(any(ConfirmationToken.class));
        verifyNoInteractions(userRepository);
    }

}