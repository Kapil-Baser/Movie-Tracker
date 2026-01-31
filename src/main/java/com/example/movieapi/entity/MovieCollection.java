package com.example.movieapi.entity;

import com.example.movieapi.model.AuthenticatedUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(exclude = {"owner", "movies"})
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movie_collection")
public class MovieCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    AppUser owner;

    @ManyToMany
    @JoinTable(
            name = "movie_collections",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "movie_id")
    )
    private Set<Movie> movies = new HashSet<>();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieCollection that = (MovieCollection) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    public void addMovie(Movie movie) {
        movies.add(movie);
    }

    public void removeMovie(Movie movie) {
        movies.remove(movie);
    }

    public boolean containsMovie(Movie movie) {
        return movies.contains(movie);
    }

    public boolean containsMovieWithId(Long movieId) {
        return movies.stream()
                .anyMatch(movie -> movie.getId().equals(movieId));
    }

    public int collectionSize() {
        return movies.size();
    }

    public String formattedCollectionSize() {
        return movies.size() < 2 ? movies.size() + " Title" : movies.size() + " Titles";
    }

}
