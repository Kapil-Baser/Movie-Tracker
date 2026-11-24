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
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(properties = "spring.quartz.auto-startup=false")
@Transactional
class MovieKeysetPaginationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:18-alpine");

    @Autowired
    MoviesRepository moviesRepository;

    @Autowired
    CollectionRepository collectionRepository;

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
}
