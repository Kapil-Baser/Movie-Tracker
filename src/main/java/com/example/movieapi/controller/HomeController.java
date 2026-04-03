package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.dto.MovieViewDto;
import com.example.movieapi.dto.PagedMovieView;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.*;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
//@RequestMapping("/movies")
public class HomeController {

    private final MovieCollectionService collectionService;
    private final MovieService movieService;
    private final MovieViewAssemblerService viewAssemblerService;
    private static final String PAGED_MOVIES = "pagedMovies";

    @Autowired
    public HomeController(MovieCollectionService collectionService, MovieService movieService, MovieViewAssemblerService viewAssemblerService) {
        this.collectionService = collectionService;
        this.movieService = movieService;
        this.viewAssemblerService = viewAssemblerService;
    }

    @ModelAttribute
    public void addUserNameToModel(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {

        if (authenticatedUser != null) {
            AppUser user = authenticatedUser.getUser();
            model.addAttribute("username", user.getUsername());
        }
    }

    @GetMapping
    public String nowPlaying(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                             Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Now Playing", 0);
        model.addAttribute(PAGED_MOVIES, pagedMovieView);
        return "now-playing";
    }

    @HxRequest
    @GetMapping("/nowPlaying/paged")
    public String nowPlayingNextPage(@RequestParam( value = "page", defaultValue = "1") int page,
                                     @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                     Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Now Playing", page);
        model.addAttribute(PAGED_MOVIES, pagedMovieView);
        return "fragments/page :: nowPlayingPage";
    }

    @GetMapping("/upcoming")
    public String upcoming(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                           Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Upcoming", 0);
        model.addAttribute(PAGED_MOVIES, pagedMovieView);
        return "upcoming";
    }

    @HxRequest
    @GetMapping("/upcoming/paged")
    public String upcomingNextPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Upcoming", page);
        model.addAttribute(PAGED_MOVIES, pagedMovieView);
        return "fragments/page :: upcomingPage";
    }

    @GetMapping("/trending")
    public String trending(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                           Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Trending", 0);
        model.addAttribute(PAGED_MOVIES, pagedMovieView);
        return "trending";
    }

    @HxRequest
    @GetMapping("/trending/paged")
    public String trendingNextPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Trending", page);
        model.addAttribute(PAGED_MOVIES, pagedMovieView);
        return "fragments/page :: trendingPage";
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

    private PagedMovieView preparePagedMovieView(AuthenticatedUser authenticatedUser,
                                                 String collectionName,
                                                 int pageNumber) {
        Page<MovieDto> pagedMovies = collectionService.getPaginatedMoviesFromCollectionByName(collectionName, pageNumber, 15);
        List<MovieViewDto> movieViewDtos = viewAssemblerService.buildMovieView(authenticatedUser, pagedMovies.getContent());
        return new PagedMovieView(movieViewDtos, pagedMovies.hasNext(), pagedMovies.getNumber() + 1);
    }
}
