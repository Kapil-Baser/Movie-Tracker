package com.example.movieapi.controller;

import com.example.movieapi.dto.CollectionDto;
import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/collections")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MovieCollectionController {

    private final MovieCollectionService collectionService;


    @PostMapping("/{collectionName}/{movieId}")
    public ResponseEntity<MovieCollection> addMovie(
            @PathVariable String collectionName,
            @PathVariable Long movieId,
            Authentication auth) {

        return ResponseEntity.ok(collectionService.addMoviesToUserCollection(auth, movieId, collectionName));
    }

    @GetMapping()
    public String collectionsPage(Authentication auth, Model model) {
        List<MovieCollection> collections = collectionService.getAllUserCollection(auth);
        model.addAttribute("collections", collections);
        return "collections";
    }

/*    @GetMapping("/{collectionId}")
    public String showCollection(@PathVariable(value = "collectionId") Long collectionId,
                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                 Model model) {

        List<MovieDto> moviesInCollection = collectionService.getAllMoviesFromCollection(collectionId, authenticatedUser);
        Page<MovieDto> movieDtoPage = collectionService.getMoviesFromCollectionPaged(collectionId, 0, 3);
        model.addAttribute("movies", moviesInCollection);

        return "collection-page";
    }*/

    @GetMapping("/{collectionId}")
    public String showCollection(@PathVariable(value = "collectionId") Long collectionId,
                                 @RequestParam(value = "page", defaultValue = "1") int pageRequest,
                                 @RequestParam(value = "size", defaultValue = "3") int size,
                                 Model model) {

        int page = Math.max(0, pageRequest - 1);
        Page<MovieDto> pageOfMovies = collectionService.getMoviesFromCollectionPaged(collectionId, page, size);

        model.addAttribute("moviesPage", pageOfMovies);
        model.addAttribute("currentPage", pageRequest);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", pageOfMovies.getTotalPages());
        model.addAttribute("collectionId", collectionId);

        return "collection-page";
    }

    // HTMX endpoint: Returns just the form fragment
    @HxRequest
    @GetMapping("/add-form")
    public String showAddForm(Model model) {
        model.addAttribute("collectionDto", new CollectionDto());
        return "fragments/collection-form :: add-form";
    }

    // HTMX endpoint: Removes the form (cancel button)
    @HxRequest
    @GetMapping("/cancel-form")
    public String cancelForm() {
        return "fragments/collection-form :: empty";
    }

    @GetMapping("/new")
    public String newCollection(Model model) {
        model.addAttribute("collectionDto", new CollectionDto());
        return "new-collection";
    }

    // HTMX endpoint: Add new collection to user collections
    @PostMapping("/add")
    public String addCollection(@Valid @ModelAttribute("collectionDto") CollectionDto collectionDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                Model model) {

        if (bindingResult.hasErrors()) {
            // Returning the form with errors
            model.addAttribute("collectionDto", new CollectionDto());
            return "fragments/collection-form :: add-form";
        }

        AppUser user = authenticatedUser.getUser();

        if (collectionService.nameExists(collectionDto.getName(), user.getId())) {
            bindingResult.rejectValue("name", "name.duplicate", "This collection name already exists");
            model.addAttribute("collectionDto", new CollectionDto());
            return "fragments/collection-form :: add-form";
        }

        collectionService.createUserCollection(user, collectionDto.getName());

        return "fragments/collection-form :: collection-list";
    }

    @PostMapping("/save")
    public String saveCollection(@Valid @ModelAttribute("collectionDto") CollectionDto collectionDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        if (result.hasErrors()) {
            return "new-collection";
        }

        AppUser user = authenticatedUser.getUser();

        if (collectionService.nameExists(collectionDto.getName(), user.getId())) {
            result.rejectValue("name", "name.duplicate", "This collection name already exists");
            return "new-collection";
        }

        collectionService.createUserCollection(user, collectionDto.getName());

        return "redirect:/collections";
    }

    @GetMapping("/delete-confirmation")
    public String showDeleteConfirmation() {
        return "fragments/buttons :: confirmation-prompt";
    }

    @GetMapping("/cancel-delete")
    public String hideDeleteConfirmation() {
        return "fragments/buttons :: delete-button";
    }
}
