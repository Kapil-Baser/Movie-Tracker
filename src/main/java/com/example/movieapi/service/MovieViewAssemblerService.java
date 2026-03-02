package com.example.movieapi.service;

import com.example.movieapi.dto.*;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.model.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class MovieViewAssemblerService {

    private final MovieCollectionService collectionService;
    private final MovieSubscriptionService subscriptionService;
    private final WatchedMovieService watchedMovieService;

    @Autowired
    public MovieViewAssemblerService(MovieCollectionService collectionService, MovieSubscriptionService subscriptionService, WatchedMovieService watchedMovieService) {
        this.collectionService = collectionService;
        this.subscriptionService = subscriptionService;
        this.watchedMovieService = watchedMovieService;
    }

    public List<MovieViewDto> buildMovieView(AuthenticatedUser authenticatedUser, List<MovieDto> dtoList) {
        Set<Long> favoritedMovieIds = Set.of();
        Set<Long> subscribedMovieIds = Set.of();
        Set<Long> watchListedMovieIds = Set.of();
        Set<Long> watchedMovieIds = Set.of();

        if (authenticatedUser != null) {
            AppUser user = authenticatedUser.getUser();
            favoritedMovieIds = collectionService.getFavoritedMovieIds(user);
            subscribedMovieIds = subscriptionService.getSubscribedMovieIds(user);
            watchListedMovieIds = collectionService.getWatchListedMovieIds(user);
            watchedMovieIds = watchedMovieService.getWatchedMoviesIds(user);
        }

        Set<Long> finalFavoritedMovieIds = favoritedMovieIds;
        Set<Long> finalSubscribedMovieIds = subscribedMovieIds;
        Set<Long> finalWatchListedMovieIds = watchListedMovieIds;
        Set<Long> finalWatchedMovieIds = watchedMovieIds;

        return dtoList.stream()
                .map(movieDto -> buildMovieViewDto(movieDto,
                        finalFavoritedMovieIds,
                        finalSubscribedMovieIds,
                        finalWatchListedMovieIds,
                        finalWatchedMovieIds))
                .toList();

    }

    private MovieViewDto buildMovieViewDto(MovieDto movieDto,
                                           Set<Long> favoriteMovieIds,
                                           Set<Long> subscribedMovieIds,
                                           Set<Long> watchListedMovieIds,
                                           Set<Long> watchedMovieIds) {
        return MovieViewDto.builder()
                .movieDto(movieDto)
                .isFavorited(favoriteMovieIds.contains(movieDto.id()))
                .isInWatchList(watchListedMovieIds.contains(movieDto.id()))
                .isSubscribed(subscribedMovieIds.contains(movieDto.id()))
                .isSubscribable(MoviePolicyService.isMovieSubscribable(movieDto.releaseDate(), movieDto.usDigitalReleaseDate()))
                .isWatched(watchedMovieIds.contains(movieDto.id()))
                .build();
    }

    // TODO: Refactor this or make a collection view builder service
    public CollectionView buildAllCollectionView(AuthenticatedUser authenticatedUser, int pageNumber) {
        Pageable pageable = PageRequest.of(pageNumber, 10);
        Page<MovieCollection> movieCollectionsPage = collectionService.getAllUserCollectionPaged(authenticatedUser.getUser(), pageable);

        if (movieCollectionsPage.hasContent()) {
            List<MovieCollection> movieCollections = movieCollectionsPage.getContent();
            List<CollectionDto> collectionDtos = movieCollections.stream()
                    .map(mc -> {
                        Long id = mc.getId();
                        String name = mc.getName();
                        String size = mc.formattedCollectionSize();
                        return new CollectionDto(id, name, size);
                    }).toList();

            return new CollectionView(collectionDtos, movieCollectionsPage.hasNext(), movieCollectionsPage.getNumber() + 1);
        }

        return new CollectionView(List.of(), movieCollectionsPage.hasNext(), movieCollectionsPage.getNumber());
    }

    public List<CollectionOptionDto> buildCollectionOptions(AuthenticatedUser authenticatedUser, Long movieId) {
        // First we get all the user collections
        List<MovieCollection> collections = collectionService.getAllUserCollection(authenticatedUser.getUser());

        return collections.stream()
                .map(collection -> new CollectionOptionDto(
                        collection.getName(),
                        collection.getId(),
                        collection.containsMovieWithId(movieId))
                )
                .toList();
    }
}
