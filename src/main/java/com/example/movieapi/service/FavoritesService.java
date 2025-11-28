package com.example.movieapi.service;

import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.repository.CollectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

@Service
public class FavoritesService {

    private final CollectionRepository repository;
    private final MovieService movieService;
    private final MovieCollectionService movieCollectionService;

    @Autowired
    public FavoritesService(CollectionRepository repository, MovieService movieService, MovieCollectionService movieCollectionService) {
        this.repository = repository;
        this.movieService = movieService;
        this.movieCollectionService = movieCollectionService;
    }


    @Transactional
    public MovieCollection getFavoritesCollection(AppUser user) {

        return repository.findByOwnerIdAndName(user.getId(), "Favorites")
                .orElseGet(() -> movieCollectionService.createUserCollection(user, "Favorites"));
    }

    /**
     * Get all favorited movie IDs for a user
     */
    public Set<Long> getFavoritedMovieIds(AppUser user) {
        Set<Long> movieIds = repository
                .findAllMovieIdsByOwnerAndName(user, "Favorites");

        return movieIds != null ? movieIds : Collections.emptySet();
    }

}
