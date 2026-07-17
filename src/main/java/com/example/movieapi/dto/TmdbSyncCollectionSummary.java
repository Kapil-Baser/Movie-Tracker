package com.example.movieapi.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record TmdbSyncCollectionSummary(int totalFetchedFromTmdb,
                                        int alreadyInDatabase,
                                        int newlySaved,
                                        List<MovieDto> movies) {
}
