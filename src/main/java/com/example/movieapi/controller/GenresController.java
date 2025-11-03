package com.example.movieapi.controller;

import com.example.movieapi.service.GenresService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/genres")
public class GenresController {

    private final GenresService genresService;

    @Autowired
    public GenresController(GenresService genresService) {
        this.genresService = genresService;
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncGenres() {
        genresService.syncGenresFromTmdb();
        return ResponseEntity.ok("Genres synced successfully");
    }
}
