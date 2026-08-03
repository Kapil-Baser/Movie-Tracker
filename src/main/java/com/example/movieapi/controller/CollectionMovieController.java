package com.example.movieapi.controller;

import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.service.MovieCollectionService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/collections/{collectionId}/movies")
public class CollectionMovieController {
    private final MovieCollectionService movieCollectionService;
    private static final int PAGE_SIZE = 3;

    @Autowired
    public CollectionMovieController(MovieCollectionService movieCollectionService) {
        this.movieCollectionService = movieCollectionService;
    }

    @GetMapping
    public String showMoviesInCollection(@PathVariable(value = "collectionId") Long collectionId,
                                         Model model, @AuthenticationPrincipal(expression = "user") AppUser owner) {

        Page<MovieDto> firstPage = movieCollectionService.getMoviesFromCollectionPaged(collectionId, 0, PAGE_SIZE, owner);
        String collectionName = movieCollectionService.getCollectionName(collectionId);

        model.addAttribute("movies", firstPage.getContent());
        model.addAttribute("currentPage", 0);
        model.addAttribute("hasNext", firstPage.hasNext());
        model.addAttribute("collectionId", collectionId);
        model.addAttribute("collectionName", collectionName);

        return "movies-in-collection";
    }

    @HxRequest
    @GetMapping
    public String showNextPage(@PathVariable(value = "collectionId") Long collectionId,
                               @RequestParam(defaultValue = "1") int page,
                               Model model, @AuthenticationPrincipal(expression = "user") AppUser owner) {
        Page<MovieDto> nextPage = movieCollectionService.getMoviesFromCollectionPaged(collectionId, page, PAGE_SIZE, owner);

        model.addAttribute("movies", nextPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("hasNext", nextPage.hasNext());
        model.addAttribute("collectionId", collectionId);

        return "fragments/page :: movieCollection";
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
