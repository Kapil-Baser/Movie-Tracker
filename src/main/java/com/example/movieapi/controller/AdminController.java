package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.dto.TmdbSyncCollectionSummary;
import com.example.movieapi.dto.YouTubeSyncSummary;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.service.MovieSyncService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/movie")
public class AdminController {

    private final MovieSyncService movieSyncService;

    public AdminController(MovieSyncService movieSyncService) {
        this.movieSyncService = movieSyncService;
    }

    @GetMapping
    public String getToken() {
        return "token";
    }

    @PostMapping("/upcoming/{page_no}")
    public ResponseEntity<TmdbSyncCollectionSummary> syncUpcomingMoviesFromTmdb(@PathVariable(name = "page_no") int page) {
        TmdbSyncCollectionSummary result = movieSyncService.syncUpcomingCollectionFromTmdb(page);
        if (result.movies().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    // TODO: Should return the synced movies
    @PostMapping("/sync-release-dates")
    public ResponseEntity<String> syncReleaseDates() {
        movieSyncService.fetchAndSyncDigitalReleaseDates();
        return ResponseEntity.ok("Synced release dates");
    }

    @GetMapping("/anticipated")
    public ResponseEntity<List<MovieDto>> syncMostAnticipated() {
        return ResponseEntity.ok(movieSyncService.syncMostAnticipated());
    }

    @PostMapping("/now-playing/{page_no}")
    public ResponseEntity<TmdbSyncCollectionSummary> syncNowPlayingCollectionFromTmdb(@PathVariable(name = "page_no") int page) {
        TmdbSyncCollectionSummary result = movieSyncService.syncNowPlayingCollectionFromTmdb(page);
        if (result.movies().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PostMapping("/trending")
    public ResponseEntity<List<MovieDto>> trendingMoviesFromTrakt() {
        List<MovieDto> dto = movieSyncService.syncTrendingMoviesFromTrakt();
        if (dto.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/update-youtube-trailers")
    public ResponseEntity<YouTubeSyncSummary> updateMoviesWithTrailers() {
        return ResponseEntity.ok(movieSyncService.syncYouTubeTrailers());
    }

    @GetMapping("/details/{movie_id}")
    public ResponseEntity<TmdbMovieDetailsResponse> getMovieDetails(@PathVariable("movie_id") Long movieId) {
        return ResponseEntity.ok(movieSyncService.getMovieDetails(movieId));
    }

    @GetMapping("/update-runtime")
    public void updateMoviesMissingRuntime() {
        movieSyncService.updateMovieRuntime();
    }

    @GetMapping("/mostwatched/{page_no}")
    public void getMostWatchedMovies(@PathVariable("page_no") int page) {
        movieSyncService.syncMostWatchedMovies(page);
    }
}
