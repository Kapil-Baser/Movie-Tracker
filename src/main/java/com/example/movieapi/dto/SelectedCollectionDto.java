package com.example.movieapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SelectedCollectionDto {
    private Long selectedMovieId;
    private Long selectedCollectionId;
    private String selectedMovieTitle;
}
