package com.example.movieapi.controller;

import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/user")
public class WatchListCollectionController {

    private final MovieCollectionService movieCollectionService;

    @Autowired
    public WatchListCollectionController(MovieCollectionService movieCollectionService) {
        this.movieCollectionService = movieCollectionService;
    }

    @PostMapping("/collections/watchlist/toggle")
    public String watchListToggle(@RequestParam Long movieId,
                                  @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                  Model model) {
        boolean isWatchListed = movieCollectionService.toggleWatchListed(authenticatedUser, movieId);

        model.addAttribute("movieId", movieId);
        model.addAttribute("isWatchListed", isWatchListed);
        return "fragments/buttons :: watch-list-button";
    }

    @PostMapping("/watched")
    public String movieWatched(@RequestParam String movieId) {
        return "";
    }
}
