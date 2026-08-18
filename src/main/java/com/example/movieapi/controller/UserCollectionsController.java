package com.example.movieapi.controller;

import com.example.movieapi.dto.CollectionRenameDto;
import com.example.movieapi.dto.MovieCollectionView;
import com.example.movieapi.dto.SelectedCollectionDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import com.example.movieapi.service.MovieViewAssemblerService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserCollectionsController {

    private final MovieCollectionService movieCollectionService;
    private final MovieViewAssemblerService movieViewAssemblerService;

    @Autowired
    public UserCollectionsController(MovieCollectionService movieCollectionService, MovieViewAssemblerService movieViewAssemblerService) {
        this.movieCollectionService = movieCollectionService;
        this.movieViewAssemblerService = movieViewAssemblerService;
    }

    @GetMapping("/collections")
    public String showAddMovieToUserCollectionForm(@RequestParam("movie_id") Long movieId,
                                         @RequestParam("movie_title") String movieTitle,
                                         @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                         Model model) {

        List<MovieCollectionView> movieCollectionViews = movieViewAssemblerService.buildMovieCollectionView(authenticatedUser, movieId);

        // Pre-populating the movieId, which I want to carry over to the post request of form
        SelectedCollectionDto dto = new SelectedCollectionDto();
        dto.setSelectedMovieId(movieId);
        dto.setSelectedMovieTitle(movieTitle);

        model.addAttribute("movieCollectionViews", movieCollectionViews);
        model.addAttribute("selectedCollection", dto);
        return "fragments/modal :: movie-collection-form";
    }

    // TODO: Maybe here also check the authenticated user is adding a movie to a collection they own
    @HxRequest
    @PostMapping("/collections/add-movie")
    public String addMovieToCollection(@ModelAttribute SelectedCollectionDto dto, Model model) {
        Long movieId = dto.getSelectedMovieId();
        Long collectionId = dto.getSelectedCollectionId();
        String title = dto.getSelectedMovieTitle();

        movieCollectionService.addMovieToUserCollection(movieId, collectionId);
        String collectionName = movieCollectionService.getCollectionName(collectionId);

        model.addAttribute("message",  title + " added successfully to " + collectionName);
        return "fragments/toasts :: success";
    }

    @HxRequest
    @GetMapping("/collections/delete-config")
    public String showDeleteCollectionConfirmation(@RequestParam("collectionId") Long collectionId,
                                                   Model model) {
        model.addAttribute("collectionId", collectionId);
        return "fragments/buttons :: modal-delete";
    }

    @HxRequest
    @DeleteMapping("/collections/{collectionId}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long collectionId,
                                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        movieCollectionService.deleteCollectionByUserAndId(authenticatedUser.getUser(), collectionId);
        return ResponseEntity.ok().build();
    }

    @HxRequest
    @GetMapping("/collections/{collectionId}/edit")
    public String renameCollectionForm(@PathVariable(name = "collectionId") Long collectionId,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {
        String collectionName = movieCollectionService.getCollectionName(collectionId);

        CollectionRenameDto renameDto = new CollectionRenameDto(collectionName, collectionId);
        model.addAttribute("collectionId", collectionId);
        model.addAttribute("collection", renameDto);


        return "fragments/collection-card :: collectionRename";
    }

    @HxRequest
    @PutMapping("/collections/{collectionId}")
    public String renameCollection(@Valid @ModelAttribute("collection") CollectionRenameDto renameDto,
                                   BindingResult bindingResult,
                                   @PathVariable Long collectionId,
                                   @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                   Model model) {

        if (bindingResult.hasErrors()) {
            // Returning the form with errors
            model.addAttribute("collectionId", collectionId);
            model.addAttribute("collection", renameDto);
            return "fragments/collection-card :: collectionRename";
        }

        AppUser user = authenticatedUser.getUser();

        if (movieCollectionService.nameExists(renameDto.getName(), user.getId())) {
            bindingResult.rejectValue("name", "name.duplicate", "This collection name already exists");
            model.addAttribute("collection", renameDto);
            return "fragments/collection-card :: collectionRename";
        }
        return "fragments/collection-card :: collectionName";
    }

    @HxRequest
    @GetMapping("/collections/{collectionId}/edit/cancel")
    public String cancelCollectionRename(@PathVariable Long collectionId, Model model) {
        String collectionName = movieCollectionService.getCollectionName(collectionId);
        model.addAttribute("collectionName", collectionName);
        model.addAttribute("collectionId", collectionId);
        return "fragments/collection-card :: collectionName";
    }
}
