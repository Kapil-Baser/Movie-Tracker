package com.example.movieapi.controller;

import com.example.movieapi.model.PagedResults;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.TmdbDiscoverResponse;
import com.example.movieapi.service.TmdbService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final TmdbService tmdbService;

    public ApiController(TmdbService tmdbService) {
        this.tmdbService = tmdbService;
    }

    @GetMapping("/trending/{time_window}")
    public ResponseEntity<PagedResults> trendingMovies(@PathVariable("time_window") String timeWindow) {
        return ResponseEntity.ok(tmdbService.getTrendingMoviesByDayOrWeek(timeWindow));
    }

    @GetMapping("/movie/release-dates/{movie_id}")
    public ResponseEntity<TmdbReleaseDatesResponse> getReleaseDates(@PathVariable("movie_id") Long movieId) {
        return ResponseEntity.ok(tmdbService.getReleaseDatesByMovieId(movieId));
    }

    @GetMapping("/movie/trending/horror")
    public ResponseEntity<TmdbDiscoverResponse> trendingHorrorMovies() {
        return ResponseEntity.ok(tmdbService.getTrendingHorrorMovies());
    }
}
