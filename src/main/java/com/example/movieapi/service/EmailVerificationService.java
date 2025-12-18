package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import com.example.movieapi.exception.ExpiredTokenException;
import com.example.movieapi.exception.InvalidTokenException;
import com.example.movieapi.exception.UserAlreadyVerifiedException;
import com.example.movieapi.repository.ConfirmationTokenRepository;
import com.example.movieapi.repository.UserRepository;
import com.example.movieapi.utility.TokenHashUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

import java.util.List;


@Service
public class EmailVerificationService {

    private final ConfirmationTokenRepository repository;
    private final UserRepository userRepository;

    @Autowired
    public EmailVerificationService(ConfirmationTokenRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public String createToken(String email) {
        // Getting the user
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Revoking previous active/unconfirmed tokens
        List<ConfirmationToken> active = repository.findAllByUserAndConfirmedAtIsNullAndRevokedFalse(user);
        for (ConfirmationToken t : active) {
            t.setRevoked(true);
            t.setConfirmedAt(LocalDateTime.now());
        }
        repository.saveAll(active);

        String raw = TokenHashUtil.generateRawToken();
        String hash = TokenHashUtil.getHashedToken(raw);

        ConfirmationToken token = new ConfirmationToken();
        token.setTokenHash(hash);
        token.setRevoked(false);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(60));
        token.setUser(user);

        repository.save(token);

        return raw;
    }

    @Transactional
    public void confirmAndEnable(String rawToken) {
        String hash = TokenHashUtil.getHashedToken(rawToken);

        ConfirmationToken token = repository.findByTokenHash(hash)
                .orElseThrow(() -> new InvalidTokenException("This token is invalid"));


        if (isConfirmed(token)) {
            throw new UserAlreadyVerifiedException("Your email is already confirmed");
        }
        if (isExpired(token)) {
            throw new ExpiredTokenException("This token has expired");
        }

        // Confirming the token and setting as revoked
        token.setConfirmedAt(LocalDateTime.now());
        token.setRevoked(true);
        repository.save(token);

        // Enable user
        AppUser user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);
    }

    public ConfirmationToken findByUser(AppUser user) {
        return repository.findByUser(user).orElseThrow(() -> new IllegalArgumentException("Token not found"));
    }

    private boolean isExpired(ConfirmationToken token) {
        return token.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private boolean isConfirmed(ConfirmationToken token) {
        return token.isRevoked() || token.getConfirmedAt() != null;
    }
}
