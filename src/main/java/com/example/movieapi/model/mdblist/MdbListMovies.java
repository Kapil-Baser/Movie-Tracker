package com.example.movieapi.model.mdblist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MdbListMovies {

    private List<MdbListMovie> movies;
    private MdbListPagination pagination;
}
