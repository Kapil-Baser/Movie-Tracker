package com.example.movieapi.dto;

import java.util.List;

public record CollectionView(List<CollectionDto> collections, boolean hasNext, int nextPage) {
}
