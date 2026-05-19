package com.example.movieapi.dto;

import lombok.Builder;

@Builder
public record YouTubeSyncSummary(long moviesScanned, long trailersUpdated, long trailersNotFound, long failures) {
}
