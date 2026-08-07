package com.example.movieapi.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MoviesInCollectionView(CollectionDto collection, List<MovieDto> movies, int currentPage, boolean hasNext) {
}
