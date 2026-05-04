package com.example.movieapi.service;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.CollectionRepository;
import com.example.movieapi.repository.MoviesRepository;
import com.example.movieapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class MovieCollectionService {

    private final CollectionRepository collectionRepository;
    private final MovieMapper movieMapper;
    private final UserRepository userRepository;
    private final MovieService movieService;
    private final MoviesRepository moviesRepository;
    private static final String FAVORITES = "Favorites";
    private static final String WATCHLIST = "WatchList";

    @Autowired
    public MovieCollectionService(CollectionRepository collectionRepository, MovieMapper movieMapper, UserRepository userRepository, MovieService movieService, MoviesRepository moviesRepository) {
        this.collectionRepository = collectionRepository;
        this.movieMapper = movieMapper;
        this.userRepository = userRepository;
        this.movieService = movieService;
        this.moviesRepository = moviesRepository;
    }

    @Transactional
    public MovieCollection addMoviesToCollection(String name, List<Movie> movies) {

        MovieCollection collection = getOrCreateCollection(name);

        collection.setMovies(new HashSet<>(movies));

        MovieCollection saved = collectionRepository.save(collection);
        log.info("Added {} movies to collection {}", movies.size(), saved.getName());

        return saved;
    }

    private MovieCollection getOrCreateCollection(String name) {
        return collectionRepository.findByName(name)
                .orElseGet(() -> {
                    MovieCollection collection = new MovieCollection();
                    collection.setName(name);
                    return collectionRepository.save(collection);
                });
    }

    private MovieCollection getOrCreateCollection(AppUser user, String name) {
        return collectionRepository.findByOwnerIdAndName(user.getId(), name)
                .orElseGet(() -> {
                    log.info("Creating {} collection for user: {}", name, user.getUsername());
                    return createUserCollection(user, name);
                });
    }

    public List<MovieCollection> getAllUserCollection(AppUser user) {
        return collectionRepository.findByOwnerId(user.getId());
    }

    public Page<MovieCollection> getAllUserCollectionPaged(AppUser user, Pageable pageable) {
        return collectionRepository.findByOwner(user, pageable);
    }

    public int getCollectionCount(AuthenticatedUser authenticatedUser) {
        List<MovieCollection> collectionList = collectionRepository.findByOwner(authenticatedUser.getUser());
        return collectionList.size();
    }

    public List<MovieDto> getAllMoviesFromCollection(String name) {
        return collectionRepository.findByName(name)
                .map(collection -> collection.getMovies().stream().toList())
                .map(movieMapper::toMovieDto)
                .orElse(new ArrayList<>());
    }

    public MovieCollection createUserCollection(AppUser user, String name) {
        MovieCollection collection = new MovieCollection();
        collection.setName(name);
        collection.setOwner(user);
        return collectionRepository.save(collection);
    }

    public MovieCollection addMoviesToUserCollection(Authentication auth, Long movieId, String collectionName) {

        AppUser user = getCurrentUser(auth);
        Movie movie = movieService.getMovieById(movieId);
        MovieCollection collection = collectionRepository.findByOwnerIdAndName(user.getId(), collectionName)
                .orElseGet(() -> createUserCollection(user, collectionName));

        if (collection.getMovies().contains(movie)) {
            throw new IllegalArgumentException("Movie already in collection");
        }

        collection.addMovie(movie);
        return collectionRepository.save(collection);
    }

    public void addMovieToUserCollection(Long movieId, Long collectionId) {
        Movie movie = movieService.getMovieById(movieId);
        MovieCollection collection = collectionRepository.getReferenceById(collectionId);

        if (collection.getMovies().contains(movie)) {
            throw new IllegalArgumentException("Movie already in collection");
        }
        collection.addMovie(movie);
        collectionRepository.save(collection);
    }

    public String getCollectionName(Long collectionId) {
         return collectionRepository.findNameById(collectionId);
    }

    private AppUser getCurrentUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean nameExists(String name, Long userId) {
        return collectionRepository.existsByNameAndOwnerId(name, userId);
    }

    @Transactional
    public boolean toggleFavorite(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();

        Movie existing = movieService.getMovieById(movieId);
        MovieCollection favoritesMoviesCollection = getOrCreateCollection(user, FAVORITES);

        boolean isFavorated = favoritesMoviesCollection.getMovies().contains(existing);

        if (isFavorated) {
            favoritesMoviesCollection.removeMovie(existing);
        } else {
            favoritesMoviesCollection.addMovie(existing);
        }

        collectionRepository.save(favoritesMoviesCollection);
        return !isFavorated;
    }

    /**
     * Get all favorited movie IDs for a user
     * if no movies exists then return empty set instead of null
     */
    public Set<Long> getFavoritedMovieIds(AppUser user) {
        Set<Long> movieIds = collectionRepository
                .findAllMovieIdsByOwnerAndName(user, FAVORITES);

        return movieIds != null ? movieIds : Collections.emptySet();
    }

    public Set<Long> getWatchListedMovieIds(AppUser user) {
        Set<Long> movieIds = collectionRepository
                .findAllMovieIdsByOwnerAndName(user, WATCHLIST);

        return movieIds != null ? movieIds : Collections.emptySet();
    }

    public Page<MovieDto> getMoviesFromCollectionPaged(Long collectionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviesPage = moviesRepository.findMoviesByCollectionId(collectionId, pageable);
        return moviesPage.map(movieMapper::toMovieDto);
    }

    public Page<MovieDto> getPaginatedMoviesFromCollectionByName(String collectionName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> pagedMovies = moviesRepository.findMoviesByCollectionNameContainingIgnoreCase(collectionName, pageable);
        return pagedMovies.map(movieMapper::toMovieDto);
    }

    public boolean toggleWatchListed(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();
        Movie movie = movieService.getMovieById(movieId);

        com.example.movieapi.entity.MovieCollection watchList = getOrCreateCollection(user, WATCHLIST);

        boolean isWatchListed = watchList.containsMovieWithId(movieId);

        if (isWatchListed) {
            watchList.removeMovie(movie);
        } else {
            watchList.addMovie(movie);
        }
        collectionRepository.save(watchList);

        return !isWatchListed;
    }

    public void addToCollection(String collectionName, List<Movie> movies) {
        MovieCollection collection = getOrCreateCollection(collectionName);

        int newlyAddedMovies =  0;
        for (Movie movie : movies) {
            if (!collection.containsMovie(movie)) {
                collection.addMovie(movie);
                newlyAddedMovies++;
            }
        }

        MovieCollection saved = collectionRepository.save(collection);
        log.info("Added {} new movies to collection {}", newlyAddedMovies, saved.getName());
    }

    @Transactional
    public void deleteCollectionByUserAndId(AuthenticatedUser authenticatedUser, Long collectionId) {
        AppUser user = authenticatedUser.getUser();
        collectionRepository.deleteByOwnerAndId(user, collectionId);
        log.info("Deleted {} collection from collection repository", collectionId);
    }
}
