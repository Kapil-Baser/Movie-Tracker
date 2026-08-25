package com.example.movieapi.controller;

import com.example.movieapi.dto.*;
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
    public ResponseEntity<TmdbSyncCollectionSummary> syncUpcomingCollectionFromTmdb(@PathVariable(name = "page_no") int page) {
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

    @PatchMapping("/update-youtube-trailers")
    public ResponseEntity<YouTubeSyncSummary> updateMoviesWithTrailers() {
        return ResponseEntity.ok(movieSyncService.syncYouTubeTrailersFromMdbList());
    }

    @GetMapping("/details/{movie_id}")
    public ResponseEntity<TmdbMovieDetailsResponse> getMovieDetails(@PathVariable("movie_id") Long movieId) {
        return ResponseEntity.ok(movieSyncService.getMovieDetails(movieId));
    }

    @PatchMapping("/update-runtime")
    public ResponseEntity<MovieRuntimeUpdateSummary> updateMoviesMissingRuntime() {
        return ResponseEntity.ok(movieSyncService.updateMovieRuntime());
    }

    @PatchMapping("/update-rating")
    public ResponseEntity<Void> updateMovieRating() {
        movieSyncService.updateMovieRating();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/update-digital-release-date")
    public ResponseEntity<DigitalReleaseSummary> updateDigitalReleaseDateFromMdbList() {
        return ResponseEntity.ok(movieSyncService.updateDigitalRelease());
    }

    @GetMapping("/mostwatched/{page_no}")
    public void getMostWatchedMovies(@PathVariable("page_no") int page) {
        movieSyncService.syncMostWatchedMovies(page);
    }

    @PostMapping("/mdblist/lists/{username}/{listName}/items")
    public ResponseEntity<MdbListSyncResult> getMovieListFromMdbList(@PathVariable String username, @PathVariable String listName,
                                                                     @RequestParam(required = false) String nextCursor) {
        return ResponseEntity.ok(movieSyncService.importAndSyncListFromMdbList(username, listName, nextCursor));
    }
}
