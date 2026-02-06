package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.PasswordResetToken;
import com.example.movieapi.exception.ExpiredTokenException;
import com.example.movieapi.exception.InvalidTokenException;
import com.example.movieapi.repository.PasswordResetTokenRepository;
import com.example.movieapi.utility.TokenHashUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PasswordResetTokenService {

    private final PasswordResetTokenRepository repository;
    private final UserService userService;

    public PasswordResetTokenService(PasswordResetTokenRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }


    @Transactional
    public String createToken(String email) {

        AppUser user;
        try {
            user = userService.loadUserByEmail(email);
        } catch (UsernameNotFoundException _) {
            log.warn("Password reset requested for non-existent email: {}", email);
            return null;
        }

        List<PasswordResetToken> active = repository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(user);
        for (PasswordResetToken token : active) {
            token.setRevoked(true);
            token.setConfirmedAt(LocalDateTime.now());
        }
        repository.saveAll(active);

        String raw = TokenHashUtil.generateRawToken();
        String hash = TokenHashUtil.getHashedToken(raw);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .tokenHash(hash)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusMinutes(120))
                .revoked(false)
                .user(user)
                .build();

        repository.save(resetToken);

        return raw;
    }

    public void validateToken(String rawToken) {
        String hash = TokenHashUtil.getHashedToken(rawToken);
        // Check if exists
        PasswordResetToken passwordResetToken = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("Invalid token"));

        if (isExpired(passwordResetToken)) {
            throw new ExpiredTokenException("Token has expired");
        }

        if (isConfirmed(passwordResetToken)) {
            throw new ExpiredTokenException("Token has expired");
        }

        // Revoke the token
        passwordResetToken.setRevoked(true);
        passwordResetToken.setConfirmedAt(LocalDateTime.now());
        repository.save(passwordResetToken);
    }

    private boolean isExpired(PasswordResetToken token) {
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean isConfirmed(PasswordResetToken token) {
        return token.getConfirmedAt() != null;
    }

    public AppUser getUser(String token) {
        String tokenHash = TokenHashUtil.getHashedToken(token);

        return repository.findByTokenHash(tokenHash)
                .map(PasswordResetToken::getUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
