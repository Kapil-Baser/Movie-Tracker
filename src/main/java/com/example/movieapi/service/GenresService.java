package com.example.movieapi.service;

import com.example.movieapi.entity.Genre;
import com.example.movieapi.model.TmdbGenreResponse;
import com.example.movieapi.repository.GenresRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenresService {

    private final Logger logger = LoggerFactory.getLogger(GenresService.class);

    private final GenresRepository genresRepository;
    private final RestClient restClient;

    @Autowired
    public GenresService(GenresRepository genresRepository, RestClient restClient) {
        this.genresRepository = genresRepository;
        this.restClient = restClient;
    }


    public List<Genre> getAllGenres() {
        return genresRepository.findAll();
    }

    public void syncGenresFromTmdb() {

        long existingCount = genresRepository.count();

        if (existingCount > 0) {
            logger.info("Genres already exist in database ({}). Skipping sync.", existingCount);
            return;
        }

        logger.info("Fetching genres from TMDB API...");

        try {

            TmdbGenreResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/genre/movie/list").build())
                    .retrieve()
                    .body(TmdbGenreResponse.class);

            if (response == null || response.getTmdbGenres() == null || response.getTmdbGenres().isEmpty()) {
                logger.warn("No genres returned from TMDB API");
                return;
            }

            // Transforming TMDB genres to entity genres
            List<Genre> genres = response.getTmdbGenres().stream()
                    .map(tmdbGenre -> {
                        Genre genre = new Genre();
                        genre.setId(tmdbGenre.getId());
                        genre.setName(tmdbGenre.getName());
                        return genre;
                    })
                            .toList();

            // Save all genres to database
            genresRepository.saveAll(genres);

            logger.info("Successfully synced {} genres from TMDB", genres.size());


        } catch (Exception e) {
            logger.info("Failed to sync genres from TMDB", e);
            throw new RuntimeException("Genre sync failed", e);
        }
    }
}
