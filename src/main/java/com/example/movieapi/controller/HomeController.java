package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDetailsDto;
import com.example.movieapi.model.PagedResults;
import com.example.movieapi.service.MovieService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class HomeController {

    private final MovieService movieService;

    public HomeController(MovieService movieService) {
        this.movieService = movieService;
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
    public PagedResults trendingMovies(@PathVariable("time_window") String timeWindow) {
        return movieService.getTrendingMovies(timeWindow);
    }
}
