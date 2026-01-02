package com.example.movieapi.controller;

import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.model.trakt.response.TraktMostAnticipatedResponse;
import com.example.movieapi.service.MovieSyncService;
import com.example.movieapi.service.TraktService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trakt/movies")
public class TraktApiController {

    private final TraktService traktService;
    private final MovieSyncService movieSyncService;

    @Autowired
    public TraktApiController(TraktService traktService, MovieSyncService movieSyncService) {
        this.traktService = traktService;
        this.movieSyncService = movieSyncService;
    }

    @GetMapping("/trending")
    public ResponseEntity<String> syncTrendingMovies() {
        return ResponseEntity.ok(traktService.getTrendingMoviesApi());
    }


    @GetMapping("/popular")
    public ResponseEntity<List<TraktMovie>> popularMovies() {
        List<TraktMovie> response = traktService.getPopularMovies();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/releases/{movie_id}")
    public ResponseEntity<String> movieReleaseDates(@PathVariable(name = "movie_id") String movieId) {
        return ResponseEntity.ok(traktService.getReleaseDates(movieId));
    }

    @GetMapping("/anticipated")
    public ResponseEntity<List<TraktMostAnticipatedResponse>> mostAnticipated() {
        return ResponseEntity.ok(traktService.getAnticipated());
    }
}
