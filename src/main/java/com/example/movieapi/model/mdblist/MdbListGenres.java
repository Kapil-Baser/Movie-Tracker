package com.example.movieapi.model.mdblist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdbListGenres {
    private int id;
    private String title;
}
