package com.example.movieapi.controller;

import com.example.movieapi.model.PagedResults;
import com.example.movieapi.model.response.MovieResultResponse;
import com.example.movieapi.model.response.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.TmdbDiscoverResponse;
import com.example.movieapi.model.response.TmdbTrendingMoviesResponse;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.model.trakt.response.TraktAllVideosResponse;
import com.example.movieapi.service.TmdbService;
import com.example.movieapi.service.TraktService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final TmdbService tmdbService;
    private final TraktService traktService;

    @Autowired
    public ApiController(TmdbService tmdbService, TraktService traktService) {
        this.tmdbService = tmdbService;
        this.traktService = traktService;
    }

    @GetMapping("/tmdb/trending/{time_window}")
    public ResponseEntity<PagedResults> trendingMovies(@PathVariable("time_window") String timeWindow) {
        return ResponseEntity.ok(tmdbService.getTrendingMoviesByDayOrWeek(timeWindow));
    }

    @GetMapping("/tmdb/release-dates/{movie_id}")
    public ResponseEntity<TmdbReleaseDatesResponse> getReleaseDates(@PathVariable("movie_id") Long movieId) {
        return ResponseEntity.ok(tmdbService.getReleaseDatesByMovieId(movieId));
    }

    @GetMapping("/tmdb/trending/horror")
    public ResponseEntity<TmdbDiscoverResponse> trendingHorrorMovies() {
        return ResponseEntity.ok(tmdbService.getTrendingHorrorMovies());
    }

    @GetMapping("/trakt/videos/{movie_id}")
    public ResponseEntity<List<TraktAllVideosResponse>> getVidoes(@PathVariable("movie_id") Long movieId) {
        return ResponseEntity.ok(traktService.getAllVideos(movieId));
    }

    @GetMapping("/tmdb/discover")
    public ResponseEntity<List<MovieResultResponse>> discoverMovie() {
        return ResponseEntity.ok(tmdbService.discoverMovies(2026, LocalDate.now().withDayOfMonth(1),4));
    }

    @GetMapping("/tmdb/now_playing/{page_no}")
    public ResponseEntity<TmdbTrendingMoviesResponse> trendingMoviesTmdb(@PathVariable(name = "page_no") Integer page) {
        return ResponseEntity.ok(tmdbService.getTrendingMovies(page));
    }

    @GetMapping("/trakt/summary/{tmdb_id}")
    public ResponseEntity<TraktMovie> getMovieSummary(@PathVariable("tmdb_id") String tmdbId) {
        return ResponseEntity.ok(traktService.getExtendedMovieDetails(tmdbId));
    }
}
