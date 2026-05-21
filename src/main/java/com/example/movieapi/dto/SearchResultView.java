package com.example.movieapi.dto;

import java.util.List;

public record SearchResultView(List<MovieDto> movies, boolean hasNext, int nextPage) {
}
