package com.example.movieapi.service;

import com.example.movieapi.dto.CollectionRenameDto;
import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.CollectionType;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.entity.Movie;
import com.example.movieapi.exception.CollectionAccessDeniedException;
import com.example.movieapi.mapper.MovieMapper;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.repository.CollectionRepository;
import com.example.movieapi.repository.MoviesRepository;
import com.example.movieapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class MovieCollectionService {

    private final CollectionRepository collectionRepository;
    private final MovieMapper movieMapper;
    private final UserRepository userRepository;
    private final MovieService movieService;
    private final MoviesRepository moviesRepository;
    private final WatchedMovieService watchedMovieService;
    private static final String FAVORITES = "Favorites";
    private static final String WATCHLIST = "WatchList";

    @Autowired
    public MovieCollectionService(CollectionRepository collectionRepository, MovieMapper movieMapper, UserRepository userRepository, MovieService movieService, MoviesRepository moviesRepository, WatchedMovieService watchedMovieService) {
        this.collectionRepository = collectionRepository;
        this.movieMapper = movieMapper;
        this.userRepository = userRepository;
        this.movieService = movieService;
        this.moviesRepository = moviesRepository;
        this.watchedMovieService = watchedMovieService;
    }

    public MovieCollection getMovieCollectionByName(String movieCollectionName) {
        return collectionRepository.findByName(movieCollectionName)
                .orElseThrow(() -> new NoSuchElementException("Movie collection with name " + movieCollectionName + " not found"));
    }

    public MovieCollection getCollectionByIdAndOwner(Long collectionId, AppUser owner) {
        return collectionRepository.findByIdAndOwner(collectionId, owner)
                .orElseThrow(() -> new NoSuchElementException("Collection with id " + collectionId + " not found"));
    }

    public int deleteStaleMoviesByCollection(String collectionName) {
        return collectionRepository.deleteStaleMoviesByCollection(collectionName);
    }

    public void addToCollection(String collectionName, CollectionType type, List<Movie> movies) {
        MovieCollection collection = getOrCreateCollection(collectionName, type);

        for (Movie movie : movies) {
            if (!collection.containsMovie(movie)) {
                collection.addMovie(movie);
            }
        }

        collectionRepository.save(collection);
    }

    public void addToNowPlayingCollection(List<Movie> movies) {
        addToCollection("Now Playing", CollectionType.NOW_PLAYING,  movies);
    }

    public void addToUpcomingCollection(List<Movie> movies) {
        addToCollection("Upcoming", CollectionType.UPCOMING,  movies);
    }

    // TODO: Make it take collection type
    private MovieCollection getOrCreateCollection(String name, CollectionType type) {
        return collectionRepository.findByName(name)
                .orElseGet(() -> {
                    MovieCollection collection = new MovieCollection();
                    collection.setName(name);
                    collection.setType(type);
                    return collectionRepository.save(collection);
                });
    }

    private MovieCollection getOrCreateCollection(AppUser user, String collectionName, CollectionType type) {
        return collectionRepository.findByOwnerIdAndName(user.getId(), collectionName)
                .orElseGet(() -> {
                    log.info("Creating {} collection for user: {}", collectionName, user.getUsername());
                    return createUserCollection(user, collectionName, type);
                });
    }

    public List<MovieCollection> getAllUserCollection(AppUser user) {
        return collectionRepository.findByOwnerId(user.getId());
    }

    public Page<MovieCollection> getAllUserCollectionPaged(AppUser user, Pageable pageable) {
        return collectionRepository.findByOwner(user, pageable);
    }

    public List<MovieCollection> sortByCollectionTypeAndThenByUpdatedAt(List<MovieCollection> collections) {
        return collections.stream()
                .sorted(Comparator.comparingInt((MovieCollection collection) ->
                                switch (collection.getType()) {
                                    case NOW_PLAYING -> 0;
                                    case UPCOMING -> 1;
                                    case WATCHLIST -> 2;
                                    case FAVORITES -> 3;
                                    case CUSTOM -> 4;
                                })
                        .thenComparing(
                                MovieCollection::getUpdatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())
                        ))
                .toList();
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

    public MovieCollection createUserCollection(AppUser user, String collectionName, CollectionType type) {
        MovieCollection collection = new MovieCollection();
        collection.setName(collectionName);
        collection.setOwner(user);
        collection.setType(type);
        return collectionRepository.save(collection);
    }

    public void addMovieToUserCollection(Long movieId, Long collectionId) {
        Movie movie = movieService.getMovieById(movieId);
        MovieCollection collection = collectionRepository.getReferenceById(collectionId);

        if (collection.containsMovie(movie)) {
            throw new IllegalArgumentException("Movie already in collection");
        }
        collection.addMovie(movie);
        collection.setUpdatedAt(LocalDateTime.now());
        collectionRepository.save(collection);
    }

    public void deleteMovieFromCollection(Long collectionId, Long movieId, AppUser owner) {
        Movie movie = movieService.getMovieById(movieId);
        MovieCollection collection = getCollectionByIdAndOwner(collectionId, owner);

        collection.removeMovie(movie);
        collection.setUpdatedAt(LocalDateTime.now());
        collectionRepository.save(collection);
    }

    @Transactional
    public void movieToWatchedHistory(Long collectionId, Long movieId, AppUser owner) {
        AppUser user = userRepository.getReferenceById(owner.getId());
        Movie movie = movieService.getMovieById(movieId);

        // First removing the movie from watchlist
        deleteMovieFromCollection(collectionId, movieId, owner);

        // Adding the movie to Watched History
        watchedMovieService.addMovieToWatchedMovies(user, movie);
    }

    public String getCollectionName(Long collectionId) {
         return collectionRepository.findNameById(collectionId);
    }

    public boolean nameExists(String name, Long userId) {
        return collectionRepository.existsByNameAndOwnerId(name, userId);
    }

    @Transactional
    public boolean toggleFavorite(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();

        Movie existing = movieService.getMovieById(movieId);
        MovieCollection favoritesMoviesCollection = getOrCreateCollection(user, FAVORITES, CollectionType.FAVORITES);

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

    public Page<MovieDto> getMoviesFromCollectionPaged(Long collectionId, int page, int size, AppUser owner) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Movie> moviesPage = moviesRepository.findMoviesByCollectionIdAndOwner(collectionId, owner, pageable);
        if (!moviesPage.hasContent()) {
            throw new CollectionAccessDeniedException("Requesting user: " + owner.getUsername() + " does not own this collection with ID: " + collectionId);
        }
        return moviesPage.map(movieMapper::toMovieDto);
    }

    public Page<MovieDto> getPaginatedMoviesFromCollectionByName(String collectionName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("releaseDate").descending());
        Page<Movie> pagedMovies = moviesRepository.findMoviesByCollectionName(collectionName, pageable);
        return pagedMovies.map(movieMapper::toMovieDto);
    }

    public boolean toggleWatchListed(AuthenticatedUser authenticatedUser, Long movieId) {
        AppUser user = authenticatedUser.getUser();
        Movie movie = movieService.getMovieById(movieId);

        MovieCollection watchList = getOrCreateCollection(user, WATCHLIST, CollectionType.WATCHLIST);

        boolean isWatchListed = watchList.containsMovieWithId(movieId);

        if (isWatchListed) {
            watchList.removeMovie(movie);
        } else {
            watchList.addMovie(movie);
        }
        collectionRepository.save(watchList);

        return !isWatchListed;
    }

    @Transactional
    public void deleteCollectionByUserAndId(AppUser owner, Long collectionId) {
        collectionRepository.deleteByOwnerAndId(owner, collectionId);
        log.info("User:{} has deleted collection with ID:{}", owner.getEmail(), collectionId);
    }

    public void renameCollection(AppUser user, @Valid CollectionRenameDto renameDto) {
        MovieCollection collection = getCollectionByIdAndOwner(renameDto.getId(), user);

        collection.setName(renameDto.getName());

        collectionRepository.save(collection);
    }
}
