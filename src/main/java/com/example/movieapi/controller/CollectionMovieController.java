package com.example.movieapi.controller;

import com.example.movieapi.dto.MoviesInCollectionView;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.service.MovieCollectionService;
import com.example.movieapi.service.MovieViewAssemblerService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/collections/{collectionId}/movies")
public class CollectionMovieController {
    private final MovieCollectionService movieCollectionService;
    private final MovieViewAssemblerService viewAssemblerService;
    private static final int PAGE_SIZE = 3;

    @Autowired
    public CollectionMovieController(MovieCollectionService movieCollectionService, MovieViewAssemblerService viewAssemblerService) {
        this.movieCollectionService = movieCollectionService;
        this.viewAssemblerService = viewAssemblerService;
    }

    @GetMapping
    public String showMoviesInCollection(@PathVariable(value = "collectionId") Long collectionId,
                                         Model model, @AuthenticationPrincipal(expression = "user") AppUser owner) {

        MoviesInCollectionView moviesInCollectionView = viewAssemblerService.buildMoviesInCollectionView(collectionId, owner, 0, PAGE_SIZE);

        model.addAttribute("collectionView", moviesInCollectionView);
        return "collection-movies";
    }

    @HxRequest
    @GetMapping
    public String showNextPage(@PathVariable(value = "collectionId") Long collectionId,
                               @RequestParam(defaultValue = "1") int page,
                               Model model, @AuthenticationPrincipal(expression = "user") AppUser owner) {
        MoviesInCollectionView moviesInCollectionView = viewAssemblerService.buildMoviesInCollectionView(collectionId, owner, page, PAGE_SIZE);

        model.addAttribute("collectionView", moviesInCollectionView);

        return "fragments/page :: collectionMoviesView";
    }

    @HxRequest
    @DeleteMapping("/{movieId}")
    public ResponseEntity<Void> deleteMovieFromCollection(@PathVariable Long collectionId,
                                                          @PathVariable Long movieId,
                                                          @AuthenticationPrincipal(expression = "user") AppUser owner) {
        movieCollectionService.deleteMovieFromCollection(collectionId, movieId, owner);
        return ResponseEntity.ok().build();
    }

    @HxRequest
    @PostMapping("/{movieId}/watch")
    public ResponseEntity<Void> moveToWatchedHistory(@PathVariable Long collectionId,
                                                     @PathVariable Long movieId,
                                                     @AuthenticationPrincipal(expression = "user") AppUser owner) {
        movieCollectionService.movieToWatchedHistory(collectionId, movieId, owner);
        return ResponseEntity.ok().build();
    }
}
