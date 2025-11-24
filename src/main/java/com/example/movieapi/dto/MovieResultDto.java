package com.example.movieapi.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MovieResultDto {
    private String title;
    private String overview;
    private String language;
    private String releaseDate;
}
