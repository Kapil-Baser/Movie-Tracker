package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.service.MovieSyncService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {

    private final MovieSyncService movieSyncService;

    public AdminController(MovieSyncService movieSyncService) {
        this.movieSyncService = movieSyncService;
    }


    @PostMapping("/movie/upcoming")
    public ResponseEntity<List<MovieDto>> upcomingMovies() {
        return ResponseEntity.ok(movieSyncService.syncUpcomingMoviesToCollection());
    }

    @PostMapping("/movie/sync-release-dates")
    public ResponseEntity<List<MovieDto>> syncReleaseDates() {
        return ResponseEntity.ok(movieSyncService.syncDigitalReleaseDates());
    }

    @PostMapping("/movie/now-playing")
    public ResponseEntity<List<MovieDto>> syncNowPlaying() {
        return ResponseEntity.ok(movieSyncService.syncNowPlayingMovies());
    }
}
