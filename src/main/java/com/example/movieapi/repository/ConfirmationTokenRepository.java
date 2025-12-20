package com.example.movieapi.repository;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.ConfirmationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfirmationTokenRepository extends JpaRepository<ConfirmationToken, Long> {

    Optional<ConfirmationToken> findByTokenHash(String tokenHash);

    Optional<ConfirmationToken> findByUser(AppUser user);

    List<ConfirmationToken> findAllByUserAndConfirmedAtIsNullAndRevokedFalse(AppUser user);
}
