package com.example.movieapi.controller;

import com.example.movieapi.model.mdblist.MdbListMovies;
import com.example.movieapi.model.response.TmdbMovieDetailsResponse;
import com.example.movieapi.model.response.TmdbReleaseDatesResponse;
import com.example.movieapi.model.response.TmdbDiscoverResponse;
import com.example.movieapi.model.response.TmdbTrendingMoviesResponse;
import com.example.movieapi.model.trakt.model.TraktMovie;
import com.example.movieapi.model.trakt.response.TraktAllVideosResponse;
import com.example.movieapi.service.MdbListService;
import com.example.movieapi.service.TmdbService;
import com.example.movieapi.service.TraktService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1")
public class ApiController {

    private final TmdbService tmdbService;
    private final TraktService traktService;
    private final MdbListService mdbListService;

    @Autowired
    public ApiController(TmdbService tmdbService, TraktService traktService, MdbListService mdbListService) {
        this.tmdbService = tmdbService;
        this.traktService = traktService;
        this.mdbListService = mdbListService;
    }

    @GetMapping("/mdblist/movie/horror")
    public ResponseEntity<String> getHorrorFromMdbList() {
        return ResponseEntity.ok(mdbListService.getHorrorMovies());
    }

    @GetMapping("/mdblist/movie/list")
    public ResponseEntity<MdbListMovies> getOfficialList() {
        return ResponseEntity.ok(mdbListService.getOfficialList());
    }

    @GetMapping("/tmdb/trending/{time_window}")
    public ResponseEntity<TmdbTrendingMoviesResponse> trendingMovies(@PathVariable("time_window") String timeWindow) {
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

    @GetMapping("/tmdb/now_playing/{page_no}")
    public ResponseEntity<TmdbTrendingMoviesResponse> trendingMoviesTmdb(@PathVariable(name = "page_no") Integer page) {
        return ResponseEntity.ok(tmdbService.getTrendingMovies(page));
    }

    @GetMapping("/trakt/summary/{tmdb_id}")
    public ResponseEntity<TraktMovie> getMovieSummary(@PathVariable("tmdb_id") String tmdbId) {
        return ResponseEntity.ok(traktService.getExtendedMovieDetails(tmdbId));
    }

    @GetMapping("/tmdb/summary/{tmdb_id}")
    public ResponseEntity<TmdbMovieDetailsResponse> getTmdbSummary(@PathVariable("tmdb_id") Long tmdbId) {
        var result = tmdbService.getMovieDetailsExtended(tmdbId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/trakt/trending")
    public String getTrendingFromTrakt() {
        return traktService.getTrendingMoviesApi();
    }
}
