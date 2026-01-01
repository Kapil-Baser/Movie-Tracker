package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import com.example.movieapi.service.MovieService;
import com.example.movieapi.service.WatchedMovieService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/movies")
public class HomeController {

    private final MovieCollectionService collectionService;
    private final MovieService movieService;
    private final WatchedMovieService watchedMovieService;

    @Autowired
    public HomeController(MovieCollectionService collectionService, MovieService movieService, WatchedMovieService watchedMovieService) {
        this.collectionService = collectionService;
        this.movieService = movieService;
        this.watchedMovieService = watchedMovieService;
    }

    /**
     * Pre-populating the view with user's favorited movie ids in case user is logged in
     * @param authenticatedUser to check if user is logged in
     * @param model to add favorites movies ids and now playing movies back to template
     */
    @ModelAttribute
    public void addFavoritedAndWatchedMovieIds(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {

        if (authenticatedUser != null) {
            AppUser user = authenticatedUser.getUser();
            Set<Long> favoritedMovieIds = collectionService.getFavoritedMovieIds(user);
            Set<Long> watchedMovieIds = watchedMovieService.getWatchedMoviesIds(user);

            model.addAttribute("favoritedMovieIds", favoritedMovieIds);
            model.addAttribute("watchedMovieIds", watchedMovieIds);

            model.addAttribute("username", user.getUsername());
        }
    }

    /**
     * Home page
     * @param model to add now-playing movies as view
     * @return Now playing movies page
     */

    @GetMapping()
    public String nowPlaying(Model model) {
        List<MovieDto> nowPlaying = collectionService.getAllMoviesFromCollection("Now Playing");
        model.addAttribute("nowPlaying", nowPlaying);
        return "now-playing";
    }

    @GetMapping("/upcoming")
    public String upcoming(Model model) {
        List<MovieDto> upcoming = collectionService.getAllMoviesFromCollection("Upcoming");
        model.addAttribute("upcoming", upcoming);
        return "upcoming";
    }

    @GetMapping("/anticipated")
    public String anticipated(Model model) {
        List<MovieDto> anticipated = collectionService.getAllMoviesFromCollection("Anticipated");
        model.addAttribute("anticipatedMovies", anticipated);
        return "anticipated";
    }

    @GetMapping("/search")
    public String searchMovieByName(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<MovieDto> movieDtoList = movieService.getMoviesByKeyword(keyword);
        model.addAttribute("movies", movieDtoList);
        model.addAttribute("keyword", keyword);
        return "search-results";
    }
}
