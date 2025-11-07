package com.example.movieapi.service;

import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.repository.CollectionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class MovieCollectionService {

    private final CollectionRepository collectionRepository;

    public MovieCollectionService(CollectionRepository collectionRepository) {
        this.collectionRepository = collectionRepository;
    }

    @Transactional
    public MovieCollection addMoviesToCollection(String name, List<Movie> movies) {

        MovieCollection collection = getOrCreateCollection(name);

        collection.setMovies(new HashSet<>(movies));

        MovieCollection saved = collectionRepository.save(collection);
        log.info("Added {} movies to collection {}", movies.size(), saved.getName());

        return saved;
    }

    @Transactional
    private MovieCollection getOrCreateCollection(String name) {
        return collectionRepository.findByName(name)
                .orElseGet(() -> {
                    MovieCollection collection = new MovieCollection();
                    collection.setName(name);
                    return collectionRepository.save(collection);
                });
    }

    public Set<Movie> getAllMoviesFromCollection(String name) {
        return collectionRepository.findByName(name)
                .map(MovieCollection::getMovies)
                .orElse(new HashSet<>());
    }
}
