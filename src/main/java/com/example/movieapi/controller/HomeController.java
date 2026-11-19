package com.example.movieapi.controller;

import com.example.movieapi.dto.*;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.CollectionType;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.*;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final MovieService movieService;
    private final MovieWindowService movieWindowService;
    private final MovieViewAssemblerService viewAssemblerService;
    private static final String PAGED_MOVIES = "pagedMovies";

    @Autowired
    public HomeController(MovieService movieService, MovieWindowService movieWindowService, MovieViewAssemblerService viewAssemblerService) {
        this.movieService = movieService;
        this.movieWindowService = movieWindowService;
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
        MovieWindowView windowView = movieWindowService.prepareView(authenticatedUser, CollectionType.NOW_PLAYING, null);
        model.addAttribute(PAGED_MOVIES, windowView);

        return "now-playing";
    }

    @HxRequest
    @GetMapping
    public String nowPlayingNextPage(@RequestParam String cursor,
                                     @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                     Model model) {
        MovieWindowView windowView = movieWindowService.prepareView(authenticatedUser, CollectionType.NOW_PLAYING, cursor);
        model.addAttribute(PAGED_MOVIES, windowView);

        return "fragments/page :: nowPlayingPage";
    }

    @GetMapping("/upcoming")
    public String upcoming(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                           Model model) {
        MovieWindowView windowView = movieWindowService.prepareView(authenticatedUser, CollectionType.UPCOMING, null);
        model.addAttribute(PAGED_MOVIES, windowView);
        return "upcoming";
    }

    @HxRequest
    @GetMapping("/upcoming")
    public String upcomingNextPage(@RequestParam String cursor,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {
        MovieWindowView windowView = movieWindowService.prepareView(authenticatedUser, CollectionType.UPCOMING, cursor);
        model.addAttribute(PAGED_MOVIES, windowView);
        return "fragments/page :: upcomingPage";
    }

    @GetMapping("/search")
    public String searchMovieByName(@RequestParam(value = "keyword", required = false) String keyword,
                                    Model model,
                                    @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        SearchResultView searchResultView = prepareSearchResultsView(authenticatedUser, keyword, 0);
        model.addAttribute("searchResults", searchResultView);
        model.addAttribute("keyword", keyword);
        return "search-results";
    }

    @GetMapping("/search/next")
    public String searchResultsNextPage(@RequestParam(value = "pageNumber", defaultValue = "1") int pageNumber,
                                        @RequestParam(value = "keyword")  String keyword,
                                        Model model, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        SearchResultView searchResultView = prepareSearchResultsView(authenticatedUser, keyword, pageNumber);
        model.addAttribute("searchResults", searchResultView);
        model.addAttribute("keyword", keyword);
        return "fragments/search-results :: search-results-card";
    }

    @GetMapping("/search-suggestions")
    public String searchSuggestions(@RequestParam(value = "keyword", required = false) String keyword,
                                    Model model) {
        var suggestions = movieService.searchSuggestions(keyword);
        model.addAttribute("suggestions", suggestions);
        return "fragments/search-results :: search-suggestions";
    }

    private SearchResultView prepareSearchResultsView(AuthenticatedUser authenticatedUser, String keyword, int pageNumber) {
        Slice<MovieDto> searchResults = movieService.searchMovies(keyword, pageNumber);
        List<MovieViewDto> viewDtos = viewAssemblerService.buildMovieView(authenticatedUser, searchResults.getContent());
        return new  SearchResultView(viewDtos, searchResults.hasNext(), searchResults.getNumber() + 1);
    }
}
