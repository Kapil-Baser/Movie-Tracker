package com.example.movieapi.model.mdblist;

import lombok.Data;

@Data
public class MdbListMovieRating {
    private String source;
    private String value;
    private String score;
    private String votes;
}
