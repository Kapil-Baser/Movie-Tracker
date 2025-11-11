package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.repository.CollectionRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MovieCollectionService {

    private final CollectionRepository collectionRepository;
    private final MovieMapper movieMapper;

    @Autowired
    public MovieCollectionService(CollectionRepository collectionRepository, MovieMapper movieMapper) {
        this.collectionRepository = collectionRepository;
        this.movieMapper = movieMapper;
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

    public List<MovieDto> getAllMoviesFromCollection(String name) {
        return collectionRepository.findByName(name)
                .map(collection -> collection.getMovies().stream().toList())
                .map(movieMapper::toMovieDto)
                .orElse(new ArrayList<>());
    }
}
