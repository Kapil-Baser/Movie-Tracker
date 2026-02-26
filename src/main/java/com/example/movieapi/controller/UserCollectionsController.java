package com.example.movieapi.controller;

import com.example.movieapi.dto.SelectedCollectionDto;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserCollectionsController {

    private final MovieCollectionService movieCollectionService;

    public UserCollectionsController(MovieCollectionService movieCollectionService) {
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

    @GetMapping("/collections")
    public String showUserCollectionForm(@RequestParam("movie_id") Long movieId,
                                         @RequestParam("movie_title") String movieTitle,
                                         @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                         Model model) {

        List<MovieCollection> collections = movieCollectionService.getAllUserCollection(authenticatedUser.getUser());

        // Pre-populating the movieId, which I want to carry over to the post request of form
        SelectedCollectionDto dto = new SelectedCollectionDto();
        dto.setSelectedMovieId(movieId);
        dto.setSelectedMovieTitle(movieTitle);

        model.addAttribute("userCollections", collections);
        model.addAttribute("selectedCollection", dto);
        return "fragments/modal :: movie-collection-form";
    }

    @PostMapping("/collections/add-movie")
    public String addMovieToCollection(@ModelAttribute SelectedCollectionDto dto) {
        Long movieId = dto.getSelectedMovieId();
        Long collectionId = dto.getSelectedCollectionId();

        // TODO: add movie to collection
        return "redirect:/movies";
    }

    @GetMapping("/collections/delete-config")
    public String showDeleteCollectionConfirmation(@RequestParam("collection_id") Long collectionId,
                                                   Model model) {
        model.addAttribute("collectionId", collectionId);
        return "fragments/buttons :: modal-delete";
    }
}
