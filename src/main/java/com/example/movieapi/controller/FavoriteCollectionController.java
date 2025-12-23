package com.example.movieapi.controller;

import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/collections/favorites")
public class FavoriteCollectionController {

    private final MovieCollectionService collectionService;

    public FavoriteCollectionController(MovieCollectionService collectionService) {
        this.collectionService = collectionService;
    }


    @PostMapping("/{movie_id}")
    public String addOrRemoveFromFavorites(@PathVariable(name = "movie_id") Long movieId,
                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                 Model model) {

        boolean isFavorited =  collectionService.toggleFavorite(authenticatedUser, movieId);

        model.addAttribute("movieId", movieId);
        model.addAttribute("isFavorited", isFavorited);

        return "fragments/buttons :: fav-button";
    }
}
