package com.example.movieapi.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "movie_subscriptions",  uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "movie_id"})
})
public class MovieSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @CreationTimestamp
    @Column(name = "subscribed_at", nullable = false)
    private LocalDateTime subscribedAt;

    @Column(name = "notified")
    private boolean notified;

    @Column(name = "notification_sent_at")
    private LocalDateTime notificationSentAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;
        MovieSubscription that = (MovieSubscription) o;

        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        return Objects.equals(user, that.user) && Objects.equals(movie, that.movie);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
