package com.example.movieapi.controller;

import com.example.movieapi.service.WatchedMovieService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/movies/history")
public class WatchedHistoryController {

    private final WatchedMovieService watchedMovieService;


    public WatchedHistoryController(WatchedMovieService watchedMovieService) {
        this.watchedMovieService = watchedMovieService;
    }


}
