package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.repository.ConfirmationTokenRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class TokenService {

    private final ConfirmationTokenRepository tokenRepository;

    public TokenService(ConfirmationTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public Optional<ConfirmationToken> findByToken(String token) {
        return tokenRepository.findByToken(token);
    }

    public ConfirmationToken save(ConfirmationToken token) {
        return tokenRepository.save(token);
    }

    public ConfirmationToken generateConfirmationToken(AppUser user) {
        ConfirmationToken token = new ConfirmationToken();
        token.setToken(UUID.randomUUID().toString());
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        token.setUser(user);

        return tokenRepository.save(token);
    }

    private boolean isExpired(ConfirmationToken token) {
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean isVerified(ConfirmationToken token) {
        LocalDateTime confirmedAt = token.getConfirmedAt();
        return confirmedAt != null;
    }

    public void confirmToken(String token) {

        ConfirmationToken confirmationToken = findByToken(token)
                .orElseThrow(() -> new IllegalStateException("Invalid token"));

        if (isVerified(confirmationToken)) {
            throw new IllegalStateException("User is already verified");
        }

        if (isExpired(confirmationToken)) {
            throw new IllegalStateException("Token has expired, please generate a new token");
        }

        // Update the confirmation time
        confirmationToken.setConfirmedAt(LocalDateTime.now());

        tokenRepository.save(confirmationToken);

    }
}
