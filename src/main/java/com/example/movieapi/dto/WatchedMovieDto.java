package com.example.movieapi.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WatchedMovieDto {
    private String title;
    private String posterPath;
    private String movieId;
    private String runtime;
    private String certification;
    private Set<String> genres;
    private LocalDate watchedAt;
    private String releaseDate;
    private BigDecimal rating;
    private Long votes;
}
