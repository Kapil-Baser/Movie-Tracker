package com.example.movieapi.dto;

import com.example.movieapi.entity.CollectionType;
import lombok.Builder;

@Builder
public record CollectionDto(Long id, String name, CollectionType type, String formattedSize) {
}
