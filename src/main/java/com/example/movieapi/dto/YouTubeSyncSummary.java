package com.example.movieapi.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record YouTubeSyncSummary(
        long moviesScanned,
        long trailersUpdated,
        List<Long> foundTmdbIds,
        long trailersNotFound,
        List<Long> notFoundTmdbIds,
        long failures,
        List<Long> failureTmdbIds) {
}
