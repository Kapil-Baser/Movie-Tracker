package com.example.movieapi.dto;

import java.util.List;

public record PagedMovieView(List<MovieViewDto> movies, boolean hasNext, int nextPage) {
}
