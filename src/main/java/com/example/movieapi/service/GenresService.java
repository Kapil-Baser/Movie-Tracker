package com.example.movieapi.service;

import com.example.movieapi.entity.Genre;
import com.example.movieapi.model.TmdbGenreResponse;
import com.example.movieapi.repository.GenresRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@Slf4j
public class GenresService {

    private final GenresRepository genresRepository;
    private final RestClient restClient;

    @Autowired
    public GenresService(GenresRepository genresRepository, @Qualifier("tmdbServiceClient") RestClient restClient) {
        this.genresRepository = genresRepository;
        this.restClient = restClient;
    }


    public List<Genre> getAllGenres() {
        return genresRepository.findAll();
    }

    public void syncGenresFromTmdb() {

        long existingCount = genresRepository.count();

        if (existingCount > 0) {
            log.info("Genres already exist in database ({}). Skipping sync.", existingCount);
            return;
        }

        log.info("Fetching genres from TMDB API...");

        try {

            TmdbGenreResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/genre/movie/list").build())
                    .retrieve()
                    .body(TmdbGenreResponse.class);

            if (response == null || response.getTmdbGenres() == null || response.getTmdbGenres().isEmpty()) {
                log.warn("No genres returned from TMDB API");
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

            log.info("Successfully synced {} genres from TMDB", genres.size());


        } catch (RuntimeException e) {
            log.warn("Failed to sync genres from TMDB", e.getMessage());
        }
    }
}
