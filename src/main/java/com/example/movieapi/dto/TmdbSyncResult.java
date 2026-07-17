package com.example.movieapi.dto;

import com.example.movieapi.entity.Movie;
import lombok.Builder;

import java.util.List;

@Builder
public record TmdbSyncResult(int totalFetchedFromTmdb,
                             int alreadyInDatabase,
                             int newlySaved,
                             List<Movie> allMovies) {
    public static TmdbSyncResult empty() {
        return new TmdbSyncResult(0, 0, 0, List.of());
    }
}
