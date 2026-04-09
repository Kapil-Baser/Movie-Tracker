package com.example.movieapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    private boolean enabled;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "google_access_token", length = 500)
    private String googleAccessToken;
    @Column(name = "google_refresh_token", length = 500)
    private String googleRefreshToken;
    @Column(name = "google_token_expiry")
    private Instant googleTokenExpires;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public AppUser(String username, String email, String password, boolean enabled, Role role, Provider provider, String providerId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.enabled = enabled;
        this.provider = provider;
        this.providerId = providerId;
    }
}
