package com.example.movieapi.integration;

import com.example.movieapi.entity.Movie;
import com.example.movieapi.repository.MoviesRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.*;

@Transactional
@Testcontainers
@SpringBootTest(properties = "spring.quartz.auto-startup=false")
class TestContainersPostGreBootTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18-alpine");

    @Autowired
    MoviesRepository moviesRepository;

    @Test
    void shouldConnectToMovieRepository() {
        long count = moviesRepository.count();

        assertThat(count).isGreaterThanOrEqualTo(0);
    }

    @Test
    void shouldRetrieveMovies() {
        Movie movie = new Movie();
        movie.setTitle("Resident Evil");

        moviesRepository.save(movie);

        var movies = moviesRepository.findAll();

        assertThat(movies).hasSize(1).map(Movie::getTitle).containsExactly("Resident Evil");
    }
}
