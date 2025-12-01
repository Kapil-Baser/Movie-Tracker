package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDetailsDto;
import com.example.movieapi.dto.MovieResultDto;
import com.example.movieapi.model.PagedResults;
import com.example.movieapi.model.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.TmdbDiscoverResponse;
import com.example.movieapi.service.MovieService;
import com.example.movieapi.service.TmdbService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final MovieService movieService;
    private final TmdbService tmdbService;

    @Autowired
    public ApiController(MovieService movieService, TmdbService tmdbService) {
        this.movieService = movieService;
        this.tmdbService = tmdbService;
    }

    @GetMapping("/home")
    public String home() {
        return "Hello! This is movie database api.";
    }

    @GetMapping("/movie/{movie_id}")
    public MovieDetailsDto getMovieDetails(@PathVariable("movie_id") Long movieId) {
        return movieService.getMovieTopLevelDetailsById(movieId);
    }

    @GetMapping("/search")
    public PagedResults searchTitle(@RequestParam String title) {
        return movieService.getMovieByTitle(title);
    }

    @GetMapping("/trending/{time_window}")
    public List<MovieResultDto> trendingMovies(@PathVariable("time_window") String timeWindow) {
        return movieService.getTrendingMovies(timeWindow);
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
