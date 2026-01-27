package com.example.movieapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@Table(name = "movies")
@NoArgsConstructor
@AllArgsConstructor
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "tmdb_id", unique = true)
    private Long tmdbId;

    @Column(name = "trakt_id", unique = true)
    private Long traktId;

    @Column(name = "imdb_id", unique = true)
    private String imdbId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

    @Column(name = "tagline")
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "runtime")
    private Integer runtime;

    @Column(name = "rating")
    private BigDecimal rating;

    @Column(name = "votes")
    private Long votes;

    @Column(name = "trailer")
    private String trailer;

    private String certification;

    @Column(name = "after_credits")
    private boolean afterCredits;

    @Column(name = "during_credits")
    private boolean duringCredits;

    @Column(name = "backdrop")
    private String backdropPath;

    @Column(name = "poster")
    private String posterPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "movie_genres",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres = new HashSet<>();

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tmdb_genres", columnDefinition = "json")
    private Set<String> tmdbGenres = new HashSet<>();

    // US Release Information
    @Column(name = "us_theatrical_date")
    private LocalDate usTheatricalDate;

    @Column(name = "us_digital_date")
    private LocalDate usDigitalDate;

    @Column(name = "us_physical_date")
    private LocalDate usPhysicalDate;

    @Column(name = "us_certification", length = 20)
    private String usCertification;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Movie movie = (Movie) o;
        return id != null && id.equals(movie.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();  // Use constant hash
    }
}
