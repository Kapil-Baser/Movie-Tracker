package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.service.MovieSyncService;
import lombok.NonNull;
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
    public ResponseEntity<List<MovieDto>> syncUpcomingMoviesFromTmdb(@PathVariable(name = "page_no") int page) {
        List<MovieDto> dto = movieSyncService.syncUpcomingMovies(page);
        if (dto.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/sync-release-dates")
    public ResponseEntity<String> syncReleaseDates() {
        movieSyncService.fetchAndSyncDigitalReleaseDates();
        return ResponseEntity.ok("Synced release dates");
    }

    /*@PostMapping("/now-playing")
    public ResponseEntity<List<MovieDto>> syncNowPlaying() {
        return ResponseEntity.ok(movieSyncService.syncNowPlayingMovies());
    }*/

    @GetMapping("/anticipated")
    public ResponseEntity<List<MovieDto>> syncMostAnticipated() {
        return ResponseEntity.ok(movieSyncService.syncMostAnticipated());
    }

    @PostMapping("/now-playing/{page_no}")
    public ResponseEntity<List<MovieDto>> syncNowPlayingFromTmdb(@PathVariable(name = "page_no") int page) {
        List<MovieDto> dto = movieSyncService.syncNowPlayingMoviesFromTmdb(page);
        if (dto.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    @PostMapping("/trending")
    public ResponseEntity< @NonNull List<MovieDto>> trendingMoviesFromTrakt() {
        List<MovieDto> dto = movieSyncService.syncTrendingMoviesFromTrakt();
        if (dto.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/update-youtube-trailers")
    public ResponseEntity<String> updateMoviesWithTrailers() {
        movieSyncService.syncYouTubeTrailers();
        return ResponseEntity.ok("Updated YouTube trailers");
    }

    @GetMapping("/details/{movie_id}")
    public ResponseEntity<TmdbMovieDetailsResponse> getMovieDetails(@PathVariable("movie_id") Long movieId) {
        return ResponseEntity.ok(movieSyncService.getMovieDetails(movieId));
    }
}
