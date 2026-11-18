package com.example.movieapi.dto;

import java.util.List;

public record MovieWindowView(List<MovieViewDto> movies, boolean hasNext, String nextCursor) {
}
