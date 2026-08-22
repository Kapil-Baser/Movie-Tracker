package com.example.movieapi.model.mdblist;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class MdbListMovieRating {
    private String source;
    private BigDecimal value;
    private String score;
    private Long votes;
}
