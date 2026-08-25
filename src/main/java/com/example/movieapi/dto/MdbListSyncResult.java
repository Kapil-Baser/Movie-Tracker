package com.example.movieapi.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MdbListSyncResult(int totalFetchedFromMdbList,
                                int alreadyInDatabase,
                                int newlySaved,
                                String nextCursor,
                                List<MovieDto> allMovies) {
    public static MdbListSyncResult empty() {
        return new MdbListSyncResult(0, 0, 0, "", List.of());
    }
}
