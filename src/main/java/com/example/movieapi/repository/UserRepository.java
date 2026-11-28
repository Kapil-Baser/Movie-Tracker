package com.example.movieapi.repository;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByProviderAndProviderId(Provider provider, String providerId);

    Optional<AppUser> findByUsernameOrEmail(String username, String email);

    boolean existsByEmail(String email);
}
