package com.example.movieapi.controller;

import com.example.movieapi.dto.CollectionView;
import com.example.movieapi.dto.NewCollectionDto;
import com.example.movieapi.dto.MovieDto;
import com.example.movieapi.entity.AppUser;
import com.example.movieapi.entity.MovieCollection;
import com.example.movieapi.model.AuthenticatedUser;
import com.example.movieapi.service.MovieCollectionService;
import com.example.movieapi.service.MovieViewAssemblerService;
import io.github.wimdeblauwe.htmx.spring.boot.mvc.HxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/collections")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class MovieCollectionController {

    private final MovieCollectionService collectionService;
    private final MovieViewAssemblerService movieViewAssemblerService;
    private static final String COLLECTION_DTO = "newCollectionDto";

    @GetMapping
    public String showAllCollections(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, Model model) {
        CollectionView collectionView = movieViewAssemblerService.buildAllCollectionView(authenticatedUser, 0);
        model.addAttribute("collectionView", collectionView);
        return "all-collections";
    }

    @HxRequest
    @GetMapping("/nextPage")
    public String collectionsNextPage(@RequestParam(name = "page", defaultValue = "1") int page,
                                      @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                      Model model) {
        CollectionView collectionView = movieViewAssemblerService.buildAllCollectionView(authenticatedUser, page);
        model.addAttribute("collectionView", collectionView);
        return "fragments/page :: collectionPage";
    }

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
        model.addAttribute(COLLECTION_DTO, new NewCollectionDto());
        return "fragments/collection-form :: add-form";
    }

    // HTMX endpoint: Removes the form (cancel button)
    @HxRequest
    @GetMapping("/cancel-form")
    public String cancelForm() {
        return "fragments/collection-form :: empty";
    }

    @HxRequest
    @PostMapping("/add")
    public String addCollection(@Valid @ModelAttribute("newCollectionDto") NewCollectionDto newCollectionDto,
                                BindingResult bindingResult,
                                @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                Model model) {

        if (bindingResult.hasErrors()) {
            // Returning the form with errors
            model.addAttribute(COLLECTION_DTO, newCollectionDto);
            return "fragments/collection-form :: collection-add-form";
        }

        AppUser user = authenticatedUser.getUser();

        if (collectionService.nameExists(newCollectionDto.getName(), user.getId())) {
            bindingResult.rejectValue("name", "name.duplicate", "This collection name already exists");
            model.addAttribute(COLLECTION_DTO, newCollectionDto);
            return "fragments/collection-form :: collection-add-form";
        }

        MovieCollection collection = collectionService.createUserCollection(user, newCollectionDto.getName());

        model.addAttribute("collection", collection);

        return "redirect:htmx:/collections";
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteCollection(@RequestParam(name = "collection_id") Long collectionId, @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        collectionService.deleteCollectionByUserAndId(authenticatedUser, collectionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/save")
    public String saveCollection(@Valid @ModelAttribute("collectionDto") NewCollectionDto newCollectionDto,
                                 BindingResult result,
                                 @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        if (result.hasErrors()) {
            return "new-collection";
        }

        AppUser user = authenticatedUser.getUser();

        if (collectionService.nameExists(newCollectionDto.getName(), user.getId())) {
            result.rejectValue("name", "name.duplicate", "This collection name already exists");
            return "new-collection";
        }

        collectionService.createUserCollection(user, newCollectionDto.getName());

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
