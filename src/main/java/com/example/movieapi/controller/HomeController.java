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
@RequestMapping("/movies")
public class HomeController {

    private final MovieCollectionService collectionService;
    private final MovieService movieService;
    private final MovieViewAssemblerService viewAssemblerService;
    private static final String HAS_NEXT_PAGE = "hasNextPage";
    private static final String NEXT_PAGE_NUMBER = "nextPage";

    @Autowired
    public HomeController(MovieCollectionService collectionService, MovieService movieService, MovieViewAssemblerService viewAssemblerService) {
        this.collectionService = collectionService;
        this.movieService = movieService;
        this.viewAssemblerService = viewAssemblerService;
    }

    // TODO: Refactor this since don't need all this
    /**
     * Pre-populating the view with user's favorited movie ids in case user is logged in
     * @param authenticatedUser to check if user is logged in
     * @param model to add favorites movies ids and now playing movies back to template
     */
    @ModelAttribute
    public void addFavoritedAndWatchListedMovieIds(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {

        if (authenticatedUser != null) {
            AppUser user = authenticatedUser.getUser();
            model.addAttribute("username", user.getUsername());
        }
    }

    @GetMapping()
    public String nowPlaying(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                             Model model) {
        List<MovieDto> nowPlayingMovieDtoList = collectionService.getAllMoviesFromCollection("Now Playing");
        List<MovieViewDto> nowPlaying = viewAssemblerService.buildMovieView(authenticatedUser, nowPlayingMovieDtoList);
        model.addAttribute("nowPlaying", nowPlaying);
        return "now-playing";
    }

    private PagedMovieView preparePagedMovieView(AuthenticatedUser authenticatedUser,
                                                 String collectionName,
                                                 int pageNumber) {
        Page<MovieDto> pagedMovies = collectionService.getPaginatedMoviesFromCollectionByName(collectionName, pageNumber, 15);
        List<MovieViewDto> movieViewDtos = viewAssemblerService.buildMovieView(authenticatedUser, pagedMovies.getContent());
        return new PagedMovieView(movieViewDtos, pagedMovies.hasNext(), pagedMovies.getNumber() + 1);
    }

    @GetMapping("/upcoming")
    public String upcoming(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                           Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Upcoming", 0);
        model.addAttribute("pagedMovies", pagedMovieView);
        return "upcoming";
    }

    @HxRequest
    @GetMapping("/upcoming/paged")
    public String upcomingNextPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {
        PagedMovieView pagedMovieView = preparePagedMovieView(authenticatedUser, "Upcoming", page);
        model.addAttribute("pagedMovies", pagedMovieView);
        return "fragments/page :: page";
    }

    @GetMapping("/trending")
    public String trending(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                           Model model) {
        Page<MovieDto> trendingMoviesPaged = collectionService.getPaginatedMoviesFromCollectionByName("Trending", 0, 15);
        List<MovieViewDto> trendingMovies = viewAssemblerService.buildMovieView(authenticatedUser, trendingMoviesPaged.getContent());
        model.addAttribute("trending", trendingMovies);
        model.addAttribute(HAS_NEXT_PAGE, trendingMoviesPaged.hasNext());
        model.addAttribute(NEXT_PAGE_NUMBER, trendingMoviesPaged.getNumber() + 1);
        return "trending";
    }

    @HxRequest
    @GetMapping("/trending/paged")
    public String trendingNextPage(@RequestParam(value = "page", defaultValue = "1") int page,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {
        Page<MovieDto> trendingMoviesPaged = collectionService.getPaginatedMoviesFromCollectionByName("Trending", page, 15);
        List<MovieViewDto> trendingMovies = viewAssemblerService.buildMovieView(authenticatedUser, trendingMoviesPaged.getContent());
        model.addAttribute("movies", trendingMovies);
        model.addAttribute(HAS_NEXT_PAGE, trendingMoviesPaged.hasNext());
        model.addAttribute(NEXT_PAGE_NUMBER, trendingMoviesPaged.getNumber() + 1);
        return "fragments/movie-page :: movie-page";
    }

    /*@GetMapping("/upcoming")
    public String upcoming(Model model) {
        List<MovieDto> upcoming = collectionService.getAllMoviesFromCollection("Upcoming");
        model.addAttribute("upcoming", upcoming);
        return "upcoming";
    }*/

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
