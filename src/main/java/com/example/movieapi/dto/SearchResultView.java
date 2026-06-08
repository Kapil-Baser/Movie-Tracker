package com.example.movieapi.dto;

import java.util.List;

public record SearchResultView(List<MovieViewDto> movieCards, boolean hasNext, int nextPage) {
}
