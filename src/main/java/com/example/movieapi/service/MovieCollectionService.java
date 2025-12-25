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
    private static final String WATCHED = "Watched";
    private static final String FAVORITES = "Favorites";

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

    @Transactional
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

    public List<MovieCollection> getAllUserCollection(Authentication auth) {
        AppUser user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

        return collectionRepository.findByOwnerId(user.getId());
    }

    public List<MovieCollection> getAllUserCollection(AppUser user) {
        return collectionRepository.findByOwnerId(user.getId());
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

    private AppUser getCurrentUser(Authentication auth) {
        String username = auth.getName();
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public boolean nameExists(String name, Long userId) {
        return collectionRepository.existsByNameAndOwnerId(name, userId);
    }

    @Transactional
    public MovieCollection getFavoritesCollection(AppUser user) {

        return collectionRepository.findByOwnerIdAndName(user.getId(), FAVORITES)
                .orElseGet(() -> {
                    log.info("Creating favorites collection for user: {}", user.getUsername());
                    return createUserCollection(user, FAVORITES);
                });
    }

    @Transactional
    public MovieCollection addMovieToFavorites(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();
        Movie movie = movieService.getMovieById(movieId);

        MovieCollection favoritesMoviesCollection = getFavoritesCollection(user);

        favoritesMoviesCollection.addMovie(movie);

        return collectionRepository.save(favoritesMoviesCollection);
    }

    @Transactional
    public boolean toggleFavorite(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();

        Movie existing = movieService.getMovieById(movieId);
        // TODO: use getOrCreateCollection method instead
        MovieCollection favoritesMoviesCollection = getFavoritesCollection(user);

        // TODO: Change the condition to make only one save repo call
        if (favoritesMoviesCollection.containsMovieWithId(movieId)) {
            // Remove the movie
            favoritesMoviesCollection.removeMovie(existing);
            collectionRepository.save(favoritesMoviesCollection);
            return false; // Not favorited anymore
        } else {
            favoritesMoviesCollection.addMovie(existing);
            collectionRepository.save(favoritesMoviesCollection);
            return true; // Now favorited
        }
    }

    public boolean toggleWatched(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();

        Movie existing = movieService.getMovieById(movieId);

        MovieCollection watchedMovies = getOrCreateCollection(user, WATCHED);

        boolean isWatched = watchedMovies.containsMovieWithId(movieId);

        if (isWatched) {
            watchedMovies.removeMovie(existing);
        } else {
            watchedMovies.addMovie(existing);
        }
        collectionRepository.save(watchedMovies);

        return !isWatched;
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

    /**
     * Get all the movie ids watched by the user
     * @param user the user to inquiry
     * @return set of already watched movie ids
     */
    public Set<Long> getWatchedMovieIds(AppUser user) {
        Set<Long> movieIds = collectionRepository
                .findAllMovieIdsByOwnerAndName(user, WATCHED);

        return movieIds != null ? movieIds : Collections.emptySet();
    }

    /**
     * Helper method to get a collection given a collection id and user who owns the collection
     * @param collectionId id of collection to obtain
     * @param user user who owns the collection
     * @return collection owned by given user and collection id
     */
/*    private MovieCollection getCollectionByIdAndOwner(Long collectionId, AppUser user) {

        return collectionRepository.findByIdAndOwner(collectionId, user)
                .orElseThrow(() -> new RuntimeException("Collection not found with id: " + collectionId));

    }*/

/*    public List<MovieDto> getAllMoviesFromCollection(Long collectionId, AuthenticatedUser authenticatedUser) {
        // First we get the collection
        MovieCollection collection = getCollectionByIdAndOwner(collectionId, authenticatedUser.getUser());

        // Get the list of movies in collection
        Set<Movie> movies = collection.getMovies();

        // Convert the movie list to movie dto
        return movieMapper.toMovieDto(movies);
    }*/

    public Page<MovieDto> getMoviesFromCollectionPaged(Long collectionId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviesPage = moviesRepository.findMoviesByCollectionId(collectionId, pageable);
        return moviesPage.map(movieMapper::toMovieDto);
    }
}
