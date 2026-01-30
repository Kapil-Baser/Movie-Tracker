package com.example.movieapi.controller;

import com.example.movieapi.dto.WatchedMovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.WatchedMovieService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HtmxResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user/movies/history")
public class WatchedHistoryController {

    private final WatchedMovieService watchedMovieService;


    public WatchedHistoryController(WatchedMovieService watchedMovieService) {
        this.watchedMovieService = watchedMovieService;
    }

    @GetMapping()
    public String showWatchedMoviesHistory(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {

        Map<LocalDate, List<WatchedMovieDto>> movieHistoryMap = watchedMovieService.getWatchedMovies(authenticatedUser);
        model.addAttribute("movieHistoryMap", movieHistoryMap);
        return "watched-history";
    }

    @PostMapping("/toggle")
    public String toggleWatched(@RequestParam Long movieId,
                                @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                Model model) {
        boolean isWatched = watchedMovieService.toggleWatched(authenticatedUser, movieId);

        model.addAttribute("isWatched", isWatched);
        model.addAttribute("movieId", movieId);

        return "fragments/buttons :: mark-as-watched-unwatched-button";
    }

    @DeleteMapping("/unwatched")
    public ResponseEntity<Void> markUnwatched(@RequestParam Long movieId,
                                              @RequestParam LocalDate date,
                                              @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                              HtmxResponse htmxResponse) {
        AppUser user = authenticatedUser.getUser();
        watchedMovieService.markAsUnwatched(user, movieId);

        int moviesWatchedOnDate = watchedMovieService.getWatchedMoviesCount(user, date);
        if (moviesWatchedOnDate == 0) {
            // If there are no more movies on the given date then remove the whole container along with date that shows on page
            htmxResponse.setRetarget("closest .movies-container");
        }

        return ResponseEntity.ok().build();
    }
}
