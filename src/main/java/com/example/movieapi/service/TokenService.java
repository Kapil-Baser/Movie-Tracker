package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.repository.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenService {

    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailVerificationService emailVerificationService;
    private final ConfirmationTokenRepository tokenRepository;

    public TokenService(PasswordResetTokenService passwordResetTokenService, EmailVerificationService emailVerificationService, ConfirmationTokenRepository tokenRepository) {
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailVerificationService = emailVerificationService;
        this.tokenRepository = tokenRepository;
    }

    public ConfirmationToken findByUser(AppUser user) {
        return tokenRepository.findByUser(user).orElseThrow(() -> new IllegalArgumentException("Confirmation token not found"));
    }

    public ConfirmationToken save(ConfirmationToken token) {
        return tokenRepository.save(token);
    }


    private boolean isExpired(ConfirmationToken token) {
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean isVerified(ConfirmationToken token) {
        LocalDateTime confirmedAt = token.getConfirmedAt();
        return confirmedAt != null;
    }

    public String createEmailConfirmationToken(String email) {
        return emailVerificationService.createToken(email);
    }

    public void validateAndConfirmEmailToken(String token) {
        emailVerificationService.confirmAndEnable(token);
    }

/*    public boolean validatePasswordResetToken(String token) {
        return passwordResetTokenService.validateToken(token);
    }*/
}
