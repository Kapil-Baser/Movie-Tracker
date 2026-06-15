package com.example.movieapi.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class CollectionRefreshService {
    private final MovieCollectionService movieCollectionService;

    @Autowired
    public CollectionRefreshService(MovieCollectionService movieCollectionService) {
        this.movieCollectionService = movieCollectionService;
    }

    public void refreshUpcomingCollection() {
        log.info("Refreshing upcoming collection");
        int deletedCount = movieCollectionService.deleteStaleMoviesByCollection("Upcoming");
        log.info("Deleted {} movies from Upcoming movies collection", deletedCount);
    }
}
