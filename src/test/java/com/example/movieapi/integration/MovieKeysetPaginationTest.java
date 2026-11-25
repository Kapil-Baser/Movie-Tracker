package com.example.movieapi.integration;

import com.example.movieapi.entity.CollectionType;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.repository.CollectionRepository;
import com.example.movieapi.repository.MoviesRepository;
import com.example.movieapi.specification.MovieSpecifications;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Window;
import org.springframework.jdbc.core.JdbcTemplate;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties = "spring.quartz.auto-startup=false")
@Transactional
class MovieKeysetPaginationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    MoviesRepository moviesRepository;

    @Autowired
    CollectionRepository collectionRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private List<Movie> createMovies() {

        List<Movie> movies = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            Movie movie = new Movie();
            movie.setTitle("Test Movie " + i);
            movies.add(movie);
        }

        return moviesRepository.saveAllAndFlush(movies);
    }

    private MovieCollection createNowPlayingCollection(List<Movie> movies) {

        MovieCollection collection = new MovieCollection();
        collection.setName("Now Playing");
        collection.setType(CollectionType.NOW_PLAYING);

        movies.forEach(collection::addMovie);

        return collectionRepository.saveAndFlush(collection);
    }

    private void insertMovie(String title, LocalDateTime createdAt) {
        jdbcTemplate.update("""
                INSERT INTO movie_tracker_schema.movies
                (title, after_credits, during_credits, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?)
                """, title, false, false, createdAt, createdAt);
    }

    private int addMoviesToNowPlaying(Long collectionId) {
        return jdbcTemplate.update("""
            INSERT INTO movie_tracker_schema.movie_collections
                (collection_id, movie_id)
            SELECT ?, m.id
            FROM movie_tracker_schema.movies m
            WHERE m.title LIKE 'Timestamp Test Movie%'
            """, collectionId);
    }

    private List<Long> getExpectedIds() {
        return jdbcTemplate.queryForList("""
        SELECT id
        FROM movie_tracker_schema.movies
        WHERE title LIKE 'Timestamp Test Movie%'
        ORDER BY created_at DESC, id ASC
        """, Long.class);
    }

    @Test
    void shouldRetrieveMoviesAcrossMultipleWindows() {
        List<Movie> movies = createMovies();
        createNowPlayingCollection(movies);

        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.asc("id")
        );

        Window<Movie> firstWindow = moviesRepository.findBy(MovieSpecifications.belongsToCollection(CollectionType.NOW_PLAYING),
                specificationFluentQuery -> specificationFluentQuery
                        .sortBy(sort)
                        .limit(2)
                        .scroll(ScrollPosition.keyset()));

        assertThat(firstWindow).hasSize(2);
        assertThat(firstWindow.hasNext()).isTrue();

        ScrollPosition scrollPosition = firstWindow.positionAt(firstWindow.size() - 1);

        Window<Movie> secondWindow = moviesRepository.findBy(MovieSpecifications.belongsToCollection(CollectionType.NOW_PLAYING), specificationFluentQuery -> specificationFluentQuery
                .sortBy(sort)
                .limit(2)
                .scroll(scrollPosition));

        assertThat(secondWindow).hasSize(2);
        assertThat(secondWindow.hasNext()).isTrue();

        ScrollPosition nextScrollPosition = secondWindow.positionAt(secondWindow.size() - 1);

        Window<Movie> thirdWindow = moviesRepository.findBy(MovieSpecifications.belongsToCollection(CollectionType.NOW_PLAYING), specificationFluentQuery -> specificationFluentQuery
                .sortBy(sort)
                .limit(2)
                .scroll(nextScrollPosition));

        assertThat(thirdWindow).hasSize(1);
        assertThat(thirdWindow.hasNext()).isFalse();

        List<Long> firstWindowIds = firstWindow.getContent()
                .stream()
                .map(Movie::getId)
                .toList();

        List<Long> secondWindowIds = secondWindow.getContent()
                .stream()
                .map(Movie::getId)
                .toList();

        assertThat(firstWindowIds)
                .isNotEmpty()
                .doesNotContainAnyElementsOf(secondWindowIds);
    }

    @Test
    void shouldSortMoviesByIdWhenCreatedAtIsSame() {
        LocalDateTime sameCreatedAt = LocalDateTime.now();
        insertMovie("Timestamp Test Movie 1", sameCreatedAt);
        insertMovie("Timestamp Test Movie 2", sameCreatedAt);
        insertMovie("Timestamp Test Movie 3", sameCreatedAt);
        insertMovie("Timestamp Test Movie 4", sameCreatedAt);
        insertMovie("Timestamp Test Movie 5", sameCreatedAt.minusHours(2));

        MovieCollection collection = new MovieCollection();
        collection.setName("Test Now Playing");
        collection.setType(CollectionType.NOW_PLAYING);

        MovieCollection savedCollection =
                collectionRepository.saveAndFlush(collection);

        int associatedMovies = addMoviesToNowPlaying(savedCollection.getId());

        assertThat(associatedMovies).isEqualTo(5);

        Sort sort = Sort.by(
                Sort.Order.desc("createdAt"),
                Sort.Order.asc("id")
        );

        Window<Movie> firstWindow = moviesRepository.findBy(MovieSpecifications.belongsToCollection(CollectionType.NOW_PLAYING), specificationFluentQuery -> specificationFluentQuery
                .sortBy(sort)
                .limit(2)
                .scroll(ScrollPosition.keyset()));

        assertThat(firstWindow).hasSize(2);
        assertThat(firstWindow.hasNext()).isTrue();

        ScrollPosition nextScrollPosition = firstWindow.positionAt(firstWindow.size() - 1);

        Window<Movie> secondWindow = moviesRepository.findBy(MovieSpecifications.belongsToCollection(CollectionType.NOW_PLAYING), specificationFluentQuery -> specificationFluentQuery
                .sortBy(sort)
                .limit(2)
                .scroll(nextScrollPosition));

        assertThat(secondWindow).hasSize(2);
        assertThat(secondWindow.hasNext()).isTrue();

        ScrollPosition lastScrollPosition = secondWindow.positionAt(secondWindow.size() - 1);

        Window<Movie> thirdWindow = moviesRepository.findBy(MovieSpecifications.belongsToCollection(CollectionType.NOW_PLAYING), specificationFluentQuery -> specificationFluentQuery
                .sortBy(sort)
                .limit(2)
                .scroll(lastScrollPosition));

        assertThat(thirdWindow).hasSize(1);
        assertThat(thirdWindow.hasNext()).isFalse();

        List<Long> expectedIds = getExpectedIds();

        assertThat(expectedIds).hasSize(5);

        List<Long> actualIds = Stream.of(firstWindow, secondWindow, thirdWindow)
                .flatMap(window -> window.getContent().stream())
                .map(Movie::getId)
                .toList();

        assertThat(actualIds).containsExactlyElementsOf(expectedIds);
    }
}
