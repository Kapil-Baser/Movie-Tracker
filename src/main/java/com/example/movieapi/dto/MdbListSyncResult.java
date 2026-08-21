package com.example.movieapi.dto;

import com.example.movieapi.entity.Movie;
import lombok.Builder;

import java.util.List;

@Builder
public record MdbListSyncResult(int totalFetchedFromMdbList,
                                int alreadyInDatabase,
                                int newlySaved,
                                List<MovieDto> allMovies) {
    public static MdbListSyncResult empty() {
        return new MdbListSyncResult(0, 0, 0, List.of());
    }
}
