package com.example.movieapi.integration;

import com.example.movieapi.repository.MoviesRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
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
}
