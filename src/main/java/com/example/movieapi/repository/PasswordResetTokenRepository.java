package com.example.movieapi.repository;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    List<PasswordResetToken> findAllByUserAndConfirmedAtIsNullAndRevokedFalse(AppUser user);
}
